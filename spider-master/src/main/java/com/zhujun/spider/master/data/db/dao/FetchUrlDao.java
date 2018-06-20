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

    List<FetchUrlPo> findFetchurl(SpiderTaskPo task, int status, Date timeAfter, int limit);

    int updateFetchUrl(SpiderTaskPo task, List<String> idList, int status, Date modifyTime);

    boolean existByAction(SpiderTaskPo task, String actionId, List<Integer> statusList);

    void createIndex(SpiderTaskPo task, String[] propArray, boolean unique);
}
