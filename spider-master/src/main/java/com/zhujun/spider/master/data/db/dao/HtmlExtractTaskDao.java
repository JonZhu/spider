package com.zhujun.spider.master.data.db.dao;

import com.zhujun.spider.master.data.db.po.HtmlExtractTaskPo;

import java.util.List;

/**
 * @author zhujun
 * @desc HtmlExtractTaskDao
 * @time 2018/7/2 10:56
 */
public interface HtmlExtractTaskDao {
    void insert(HtmlExtractTaskPo task);

    HtmlExtractTaskPo getById(String taskId);

    void updateStatus(String taskId, int status);

    void setErrorInfo(String taskId, String errorInfo);

    void incrementSuccessCount(String taskId);

    void incrementFailCount(String taskId);

    void saveExtractCount(String taskId, int successCount, int failCount);

    List<HtmlExtractTaskPo> findTask();
}
