package com.zhujun.spider.master.data.db;

import com.zhujun.spider.master.data.db.po.HtmlExtractTaskPo;

import java.util.List;

/**
 * @author zhujun
 * @desc IHtmlExtractService
 * @time 2018/7/2 10:01
 */
public interface IHtmlExtractService {
    /**
     * 创建任务
     * @param task
     * @return
     */
    HtmlExtractTaskPo createExtractTask(HtmlExtractTaskPo task);

    /**
     * 改变任务状态
     *
     * @param taskId
     * @param newStatus
     */
    void chnageTaskStatus(String taskId, int newStatus);

    /**
     * 删除任务
     * @param taskId
     */
    void deleteTask(String taskId);

    /**
     * 查询任务
     * @return
     */
    List<HtmlExtractTaskPo> findTask();

    /**
     * 完成任务
     * @param taskId
     * @param errorInfo
     */
    void completeTask(String taskId, String errorInfo);
}
