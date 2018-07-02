package com.zhujun.spider.master.data.db;

import com.zhujun.spider.master.data.db.dao.HtmlExtractTaskDao;
import com.zhujun.spider.master.data.db.po.HtmlExtractTaskPo;
import com.zhujun.spider.master.extract.html.ConfigParseUtil;
import com.zhujun.spider.master.extract.html.HtmlExtractSchedule;
import com.zhujun.spider.master.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author zhujun
 * @desc HtmlExtractServiceImpl
 * @time 2018/7/2 10:39
 */

@Service
public class HtmlExtractServiceImpl implements IHtmlExtractService {

    private final static Pattern MONGO_DATA_DBNAME_PATTERN = Pattern.compile("extract_\\w+");

    @Autowired
    private HtmlExtractTaskDao htmlExtractTaskDao;

    @Autowired
    private HtmlExtractSchedule htmlExtractSchedule;

    @Override
    public HtmlExtractTaskPo createExtractTask(HtmlExtractTaskPo task) {
        // 验证mongoDbName
        if (!MONGO_DATA_DBNAME_PATTERN.matcher(task.getMongoDbName()).matches()) {
            throw new RuntimeException("mongo db name不正确");
        }

        // 验证data dir
        File dataDir = new File(task.getSrcDataDir());
        if (!dataDir.isDirectory()) {
            throw new RuntimeException("srcDataDir不存在或不为目录");
        }

        // 验证extractConfig
        validateExtractConfig(task.getExtractConfig());

        // 初始化任务信息
        task.setId(UuidUtil.create());
        task.setCreateTime(new Date());
        task.setStatus(HtmlExtractTaskPo.Status.NEW);

        // 保存任务
        htmlExtractTaskDao.insert(task);

        return task;
    }


    private void validateExtractConfig(String extractConfig) {
        // 解析不出错，则为验证通过
        try {
            ConfigParseUtil.parseJsConfig(extractConfig);
        } catch (Exception e) {
            throw new RuntimeException("html extract配置错误", e);
        }
    }

    @Override
    public void chnageTaskStatus(String taskId, int newStatus) {
        HtmlExtractTaskPo task = htmlExtractTaskDao.getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (newStatus == task.getStatus()) {
            return;
        }

        if (newStatus == HtmlExtractTaskPo.Status.PAUSED) {
            // 暂时
            pauseTask(task);
        } else if (newStatus == HtmlExtractTaskPo.Status.RUN) {
            // 启动
            runTask(task);
        } else {
            throw new RuntimeException("不能将任务设置为该状态:" + newStatus);
        }
    }

    private void runTask(HtmlExtractTaskPo task) {
        if (task.getStatus() != HtmlExtractTaskPo.Status.NEW && task.getStatus() != HtmlExtractTaskPo.Status.PAUSED) {
            throw new RuntimeException("不能启动该状态下的任务, status: " + task.getStatus());
        }

        htmlExtractSchedule.runTask(task);
        htmlExtractTaskDao.updateStatus(task.getId(), HtmlExtractTaskPo.Status.RUN);
    }

    private void pauseTask(HtmlExtractTaskPo task) {
        if (task.getStatus() != HtmlExtractTaskPo.Status.RUN) {
            throw new RuntimeException("不能暂停该状态下的任务, status: " + task.getStatus());
        }

        htmlExtractSchedule.stopTask(task.getId());
        htmlExtractTaskDao.updateStatus(task.getId(), HtmlExtractTaskPo.Status.PAUSED);
    }

    @Override
    public void deleteTask(String taskId) {
        htmlExtractSchedule.stopTask(taskId);
        htmlExtractTaskDao.updateStatus(taskId, HtmlExtractTaskPo.Status.DELETED);
    }

    @Override
    public List<HtmlExtractTaskPo> findTask() {
        return htmlExtractTaskDao.findTask();
    }

    @Override
    public void completeTask(String taskId, String errorInfo) {
        if (errorInfo == null) {
            htmlExtractTaskDao.updateStatus(taskId, HtmlExtractTaskPo.Status.COMPLETE);
        } else {
            htmlExtractTaskDao.setErrorInfo(taskId, errorInfo);
        }

        htmlExtractSchedule.stopTask(taskId);
    }
}
