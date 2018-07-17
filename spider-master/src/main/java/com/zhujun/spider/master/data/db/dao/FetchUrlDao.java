package com.zhujun.spider.master.data.db.dao;

import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;

import java.util.Date;
import java.util.List;

/**
 * @author zhujun
 * @desc FetchUrlDaoMongoImpl
 * @time 2018/6/19 11:15
 */
public interface FetchUrlDao {
    void insertFetchUrl(SpiderTaskPo task, FetchUrlPo fetchUrl);

    boolean existByUrl(SpiderTaskPo task, String fetchUrl);

    List<FetchUrlPo> findFetchurl(SpiderTaskPo task, int status, Date modifyTimeBefore, int limit);

    /**
     *
     * @param task
     * @param status
     * @param modifyTimeBefore
     * @param maxPushdownCount 最大下发次数（不包含）
     * @param limit
     * @return
     */
    List<FetchUrlPo> findFetchurl(SpiderTaskPo task, int status, Date modifyTimeBefore, Integer maxPushdownCount, int limit);

    int updateFetchUrl(SpiderTaskPo task, List<String> idList, int status, Date modifyTime);

    int updateFetchUrl(SpiderTaskPo task, List<String> idList, int status, Date modifyTime, Integer httpStatusCode);

    /**
     * 标记url为已下发状态
     *
     * @param task
     * @param idList
     * @param modifyTime
     * @return
     */
    int markFetchUrlPushed(SpiderTaskPo task, List<String> idList, Date modifyTime);

    /**
     *
     * @param task
     * @param actionId
     * @param statusList
     * @param maxPushdownCount 最大下发次数（不包含）
     * @return
     */
    boolean existByAction(SpiderTaskPo task, String actionId, List<Integer> statusList, Integer maxPushdownCount);

    void createIndex(SpiderTaskPo task, String[] propArray, boolean unique);
}
