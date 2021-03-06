package com.zhujun.spider.master.extract;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.zhujun.export.appendfile.AppendFileReader;
import com.zhujun.export.appendfile.MetaData;
import com.zhujun.spider.master.extract.html.ConfigParseUtil;
import com.zhujun.spider.master.extract.html.DataItemConfig;
import com.zhujun.spider.master.extract.html.HtmlExtractor;
import com.zhujun.spider.net.url.ContentFetcher;
import com.zhujun.spider.net.url.IFetchResult;
import com.zhujun.spider.net.url.JavaUrlContentFetcher;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.util.DigestUtils;

import javax.script.ScriptException;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlExtractorTest {

    @Test
    public void extract() throws ScriptException, IOException {
        DataItemConfig config = parseConfig("/bd_baike_ExtractConfig.js");
        HtmlExtractor extractor = new HtmlExtractor(config);
        String url = "https://baike.baidu.com/item/%E9%95%BF%E6%B1%9F%E4%B8%89%E8%A7%92%E6%B4%B2%E5%9F%8E%E5%B8%82%E7%BE%A4/5973620";
//        String url = "https://baike.baidu.com/item/%E6%9D%8E%E5%B9%BC%E6%96%8C/12503";
        ContentFetcher contentFetcher = JavaUrlContentFetcher.getInstance();
        IFetchResult fetchResult = contentFetcher.fetch(url);
        long startTime = System.currentTimeMillis();
        Object result = extractor.extract(url, fetchResult.getContentType(), fetchResult.getData());
        System.out.println("ms: " + (System.currentTimeMillis() - startTime));
    }

    @Test
    public void testParseBasicInfo() throws Exception {

        String url = "https://baike.baidu.com/item/%E9%95%BF%E6%B1%9F%E4%B8%89%E8%A7%92%E6%B4%B2%E5%9F%8E%E5%B8%82%E7%BE%A4/5973620";
        String content = IOUtils.toString(new URL(url), "UTF-8");
        long startTime = System.currentTimeMillis();
        Document document = Jsoup.parse(content, url);
//        Element basicInfoDiv = document.select(".basic-info").first();
        Elements dtElements = document.select(".basic-info dt");
        Map<String, String> data = new HashMap<>();
        for (Element dtEle : dtElements) {
            Element valueEle = dtEle.nextElementSibling();
            data.put(dtEle.text(), valueEle == null ? null : valueEle.text());
        }
        System.out.println("ms: " + (System.currentTimeMillis() - startTime));
        System.out.println(data.toString());

    }


    public DataItemConfig parseConfig(String classPath) throws ScriptException, IOException {
        InputStream configInputStream = getClass().getResourceAsStream(classPath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(configInputStream, byteArrayOutputStream);
        return ConfigParseUtil.parseJsConfig(new String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8")));
    }

    @Test
    public void testPattern() {
        Pattern fun = Pattern.compile(":((next-one|pre-one)\\(([^()]*)\\))");
        String searchString = "$name:next-one(.basicInfo-item.value)dafa:pre-one(.test p h1)";
        Matcher matcher = fun.matcher(searchString);
        while (matcher.find()) {
            System.out.println(matcher.group(3) + "|" + matcher.start() + "|" + matcher.end());
        }
    }

    @Test
    public void testExtract2Mongo() throws IOException, ScriptException {
        DataItemConfig config = parseConfig("/bd_baike_ExtractConfig.js");
        HtmlExtractor extractor = new HtmlExtractor(config);

        MongoClient mongoClient = createMongoClient();

        MongoCollection<org.bson.Document> baikeCollection = mongoClient.getDatabase("bd_baike").getCollection("baike");

        String appendDataFile = "E:\\tmp\\spider\\baike_clone\\data-20180518101708";
        AppendFileReader appendFileReader = new AppendFileReader(appendDataFile);
        MetaData metaData = null;
        int count = 0;
        while (true) {
            metaData = appendFileReader.readMetaData();
            if (metaData == null) {
                break;
            }

            if (metaData.getContentType() != null && metaData.getContentType().startsWith("text/html")) {
                byte[] content = appendFileReader.readFileData();
                if (content != null) {
                    Object extractResult = extractor.extract(metaData.getUrl(), metaData.getContentType(), content);
                    saveData2Mongo(baikeCollection, extractResult);
                    System.out.println(++count);
                }
            }
        }

        mongoClient.close();

        System.out.println("complete, count: " + count);
    }

    private final static UpdateOptions UPSERT_OPTIONS = new UpdateOptions().upsert(true);
    private static void saveData2Mongo(MongoCollection<org.bson.Document> baikeCollection, Object extractResult) {
        if (extractResult == null || !(extractResult instanceof Map)) {
            return;
        }

        org.bson.Document doc = new org.bson.Document((Map<String, Object>) extractResult);
        Object id = doc.get("_id");
        if (id != null) {
            // update
            baikeCollection.replaceOne(Filters.eq(id), doc, UPSERT_OPTIONS);
        } else {
            // insert
            baikeCollection.insertOne(doc);
        }
    }


    private MongoClient createMongoClient() {
        MongoCredential credential = MongoCredential.createScramSha1Credential("root", "admin", "qwer1234".toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress("42.123.99.34", 27017), Collections.singletonList(credential));
//        MongoClientURI uri = new MongoClientURI("mongodb://root:qwer1234@42.123.99.34:27017/?authSource=admin&authMechanism=SCRAM-SHA-1");
//        MongoClient mongoClient = new MongoClient(uri);
//        mongoClient.fsync(false);
        return mongoClient;
    }


    /**
     * 转换数据目录下所有appendFile到mongo
     */
    @Test
    public void textExtractDataDir2Mongo() throws ScriptException, IOException, InterruptedException {

        String dirPath = "E:\\tmp\\spider\\baike_clone\\";
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            throw new RuntimeException(dirPath + " 不是目录");
        }

        long startTime = System.currentTimeMillis();

        File[] dataFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("data-");
            }
        });

        if (dataFiles == null || dataFiles.length == 0) {
            return;
        }

        DataItemConfig config = parseConfig("/bd_baike_ExtractConfig.js");
        HtmlExtractor extractor = new HtmlExtractor(config);

        MongoClient mongoClient = createMongoClient();

        MongoCollection<org.bson.Document> baikeCollection = mongoClient.getDatabase("bd_baike").getCollection("baike");

        int threadCount = Runtime.getRuntime().availableProcessors() * 2 + 1;
        Executor pool = Executors.newFixedThreadPool(threadCount);
        System.out.println("创建线程池, size:" + threadCount);

        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(dataFiles.length);
        for (File appendFile : dataFiles) {
            pool.execute(new ExtractAppendFileThread(appendFile, baikeCollection, extractor, count, countDownLatch));
        }

        countDownLatch.await();

        System.out.println("time:"+ (System.currentTimeMillis() - startTime) +", count:" + count.get());
    }

    public static class ExtractAppendFileThread implements Runnable {
        private File appendFile;
        private MongoCollection<org.bson.Document> baikeCollection;
        private HtmlExtractor extractor;
        private AtomicInteger count;
        private CountDownLatch countDownLatch;

        public ExtractAppendFileThread(File appendFile, MongoCollection<org.bson.Document> baikeCollection, HtmlExtractor extractor, AtomicInteger count, CountDownLatch countDownLatch) {
            this.appendFile = appendFile;
            this.baikeCollection = baikeCollection;
            this.extractor = extractor;
            this.count = count;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            AppendFileReader appendFileReader = null;
            try {
                appendFileReader = new AppendFileReader(appendFile);
                while (true) {
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
                            saveData2Mongo(baikeCollection, extractResult);
                            System.out.println(count.incrementAndGet());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
                IOUtils.closeQuietly(appendFileReader);
            }

        }
    }

}