package com.zhujun.spider.master.extract.html;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.zhujun.export.appendfile.AppendFileReader;
import com.zhujun.export.appendfile.MetaData;
import com.zhujun.spider.master.data.db.IHtmlExtractService;
import com.zhujun.spider.master.data.db.dao.HtmlExtractTaskDao;
import com.zhujun.spider.master.data.db.po.HtmlExtractTaskPo;
import com.zhujun.spider.master.util.SpringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * html抽取任务调度
 *
 * @author zhujun
 * @desc HtmlExtractSchedule
 * @time 2018/7/2 11:30
 */

@Component
public class HtmlExtractSchedule {
    private final static Logger log = LoggerFactory.getLogger(HtmlExtractSchedule.class);

    private final static ConcurrentHashMap<String, ScheduleThread> SCHEDULE_THREAD_MAP = new ConcurrentHashMap();

    public void runTask(HtmlExtractTaskPo task) {
        AtomicBoolean isNewThread = new AtomicBoolean(false);
        ScheduleThread thread = SCHEDULE_THREAD_MAP.computeIfAbsent(task.getId(), (key)->{
            isNewThread.set(true);
            return new ScheduleThread(task);
        });

        if (isNewThread.get()) {
            // 启动新线程
            thread.start();
        }
    }

    public void stopTask(String taskId) {
        ScheduleThread thread = SCHEDULE_THREAD_MAP.remove(taskId);
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * 调度线程
     */
    private static class ScheduleThread extends Thread {
        private final static AtomicInteger SCHEDULE_THREAD_INDEX = new AtomicInteger(0);
        private final static AtomicInteger EXTRACT_THREAD_INDEX = new AtomicInteger(0);
        private final HtmlExtractTaskPo task;

        public ScheduleThread(HtmlExtractTaskPo task) {
            super((Runnable) null, "HtmlExtractScheduler-" + SCHEDULE_THREAD_INDEX.getAndIncrement());
            this.task = task;
        }

        @Override
        public void run() {
            String errorInfo = null;
            long startTime = System.currentTimeMillis();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            try {
                String dirPath = task.getSrcDataDir();
                File dir = new File(dirPath);
                if (!dir.isDirectory()) {
                    throw new RuntimeException(dirPath + " 不是目录");
                }

                File[] dataFiles = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().startsWith("data-");
                    }
                });

                if (dataFiles == null || dataFiles.length == 0) {
                    return;
                }

                DataItemConfig config = ConfigParseUtil.parseJsConfig(task.getExtractConfig());
                HtmlExtractor extractor = new HtmlExtractor(config);

                MongoCollection<Document> collection = getTaskMongoCollection(task);

                int threadCount = Math.min(Runtime.getRuntime().availableProcessors() * 2 + 1, dataFiles.length);
                Executor pool = Executors.newFixedThreadPool(threadCount, new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "HtmlExtractor-" + EXTRACT_THREAD_INDEX.getAndIncrement());
                    }
                });
                System.out.println("创建线程池, size:" + threadCount);


                CountDownLatch countDownLatch = new CountDownLatch(dataFiles.length);
                for (File appendFile : dataFiles) {
                    pool.execute(new ExtractAppendFileThread(appendFile, collection, extractor, successCount, failCount, countDownLatch));
                }

                while (countDownLatch.await(5, TimeUnit.SECONDS) == false) { // 等待子线程结束
                    saveExtractCount(task.getId(), successCount.get(), failCount.get());
                }

                // 子线程结束
                saveExtractCount(task.getId(), successCount.get(), failCount.get());
            } catch (Exception e) {
                errorInfo = ExceptionUtils.getStackTrace(e);
            } finally {
                log.info("extract html task complete, time:{}ms, successCount:{}, failCount:{}",
                        System.currentTimeMillis() - startTime, successCount.get(), failCount.get());
                SpringUtil.getContext().getBean(IHtmlExtractService.class).completeTask(task.getId(), errorInfo);
            }
        }

        private void saveExtractCount(String id, int successCount, int failCount) {
            SpringUtil.getContext().getBean(HtmlExtractTaskDao.class).saveExtractCount(id, successCount, failCount);
        }

        private MongoCollection<Document> getTaskMongoCollection(HtmlExtractTaskPo task) {
            return SpringUtil.getContext().getBean(MongoClient.class)
                    .getDatabase(task.getMongoDbName()).getCollection(task.getMongoCollectionName());
        }
    }



    /**
     * 抽取线程
     */
    public static class ExtractAppendFileThread implements Runnable {
        private File appendFile;
        private MongoCollection<org.bson.Document> collection;
        private HtmlExtractor extractor;
        private AtomicInteger successCount;
        private AtomicInteger failCount;
        private CountDownLatch countDownLatch;

        public ExtractAppendFileThread(File appendFile, MongoCollection<org.bson.Document> collection, HtmlExtractor extractor, AtomicInteger successCount, AtomicInteger failCount, CountDownLatch countDownLatch) {
            this.appendFile = appendFile;
            this.collection = collection;
            this.extractor = extractor;
            this.successCount = successCount;
            this.failCount = failCount;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            AppendFileReader appendFileReader = null;
            try {
                appendFileReader = new AppendFileReader(appendFile);
                while (true) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    try {
                        MetaData metaData = appendFileReader.readMetaData();
                        if (metaData == null) {
                            break;
                        }

                        if (metaData.getContentType() != null && metaData.getContentType().startsWith("text/html")) {
                            byte[] content = appendFileReader.readFileData();
                            if (content != null) {
                                Object extractResult = extractor.extract(metaData.getUrl(), metaData.getContentType(), content);
                                if (extractResult instanceof Map) {
                                    Map map = (Map) extractResult;
                                    map.put("_id", DigestUtils.md5DigestAsHex(metaData.getUrl().getBytes(Charset.forName("utf-8")))); // 使用url md5为id
                                    map.put("url", metaData.getUrl());
                                    map.put("contentType", metaData.getContentType());
                                    map.put("fetchTime", metaData.getFetchTime());
                                }
                                saveData2Mongo(collection, extractResult);
                                successCount.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        log.error("html extract error", e);
                        failCount.incrementAndGet();
                    }
                }
            } catch (Exception e) {
                log.error("html extract error", e);
            } finally {
                countDownLatch.countDown();
                IOUtils.closeQuietly(appendFileReader);
            }

        }

        private final static UpdateOptions UPSERT_OPTIONS = new UpdateOptions().upsert(true);
        private static void saveData2Mongo(MongoCollection<org.bson.Document> collection, Object data) {
            if (data == null) {
                return;
            }

            if (data instanceof Map) {
                saveMap2Mongo(collection, (Map) data);
            } else if (data instanceof Collection) {
                Collection dataColl = (Collection) data;
                for (Object item : dataColl) {
                    saveData2Mongo(collection, item);
                }
            } else if (data instanceof Object[]) {
                Object[] dataArray = (Object[]) data;
                for (Object item : dataArray) {
                    saveData2Mongo(collection, item);
                }
            } else {
                log.warn("忽略的数据类型:{}，未存入mongo", data.getClass().getName());
            }
        }

        private static void saveMap2Mongo(MongoCollection<org.bson.Document> collection, Map map) {
            org.bson.Document doc = new org.bson.Document(map);
            Object id = doc.get("_id");
            if (id != null) {
                // update
                collection.replaceOne(Filters.eq(id), doc, UPSERT_OPTIONS);
            } else {
                // insert
                collection.insertOne(doc);
            }
        }
    }

}
