package com.zhujun.spider.master.data.db.dao;

import com.zhujun.spider.master.data.db.Page;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;

import java.util.List;

/**
 * @author zhujun
 * @desc SpiderTaskDao
 * @time 2018/6/19 11:14
 */
public interface SpiderTaskDao {
    int countByDatadir(String dataDir);

    void insertSpiderTaskPo(SpiderTaskPo taskPo);

    Page<SpiderTaskPo> pagingTask(int pageNo, int pageSize);

    void deleteTask(String taskId);

    /**
     * 查询存在调度中的任务
     */
    List<SpiderTaskPo> findScheduledTask();

    SpiderTaskPo getTaskById(String taskId);

    int updateTaskStatus(String taskId, int status);
}
