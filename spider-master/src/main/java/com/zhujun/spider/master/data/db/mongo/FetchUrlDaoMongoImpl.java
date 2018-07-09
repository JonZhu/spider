package com.zhujun.spider.master.data.db.mongo;

import com.zhujun.spider.master.data.db.dao.FetchUrlDao;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author zhujun
 * @desc FetchUrlDaoMongoImpl
 * @time 2018/6/19 16:10
 */
@Repository
public class FetchUrlDaoMongoImpl implements FetchUrlDao {

    @Autowired
    TaskMongoTemplateGetter taskMongoTemplateGetter;

    @Override
    public void insertFetchUrl(SpiderTaskPo task, FetchUrlPo fetchUrl) {
        MongoTemplate mongoTemplate = taskMongoTemplateGetter.getTemplate(task);
        mongoTemplate.insert(fetchUrl);
    }

    @Override
    public boolean existByUrl(SpiderTaskPo task, String fetchUrl) {
        MongoTemplate mongoTemplate = taskMongoTemplateGetter.getTemplate(task);
        return mongoTemplate.exists(Query.query(Criteria.where("url").is(fetchUrl)), FetchUrlPo.class);
    }

    @Override
    public List<FetchUrlPo> findFetchurl(SpiderTaskPo task, int status, Date modifyTimeBefore, int limit) {
        MongoTemplate mongoTemplate = taskMongoTemplateGetter.getTemplate(task);
        Criteria criteria = Criteria.where("status").is(status);
        if (modifyTimeBefore != null) {
            criteria.and("modifyTime").lt(modifyTimeBefore);
        }
        return mongoTemplate.find(Query.query(criteria).limit(limit), FetchUrlPo.class);
    }

    @Override
    public int updateFetchUrl(SpiderTaskPo task, List<String> idList, int status, Date modifyTime) {
        return updateFetchUrl(task, idList, status, modifyTime, null);
    }

    public int updateFetchUrl(SpiderTaskPo task, List<String> idList, int status, Date modifyTime, Integer httpStatusCode) {
        MongoTemplate mongoTemplate = taskMongoTemplateGetter.getTemplate(task);
        Criteria criteria = Criteria.where("_id").in(idList);
        Update update = Update.update("status", status).set("modifyTime", modifyTime);
        if (httpStatusCode != null) {
            update.set("httpStatus", httpStatusCode);
        }
        return (int)mongoTemplate.updateMulti(Query.query(criteria), update, FetchUrlPo.class).getModifiedCount();
    }

    @Override
    public int markFetchUrlPushed(SpiderTaskPo task, List<String> idList, Date modifyTime) {
        MongoTemplate mongoTemplate = taskMongoTemplateGetter.getTemplate(task);
        Criteria criteria = Criteria.where("_id").in(idList);
        Update update = Update.update("status", FetchUrlPo.STATUS_PUSHED)
                .set("modifyTime", modifyTime)
                .inc("pushDownCount", 1); // 增加下发次数
        return (int)mongoTemplate.updateMulti(Query.query(criteria), update, FetchUrlPo.class).getModifiedCount();
    }

    @Override
    public boolean existByAction(SpiderTaskPo task, String actionId, List<Integer> statusList) {
        MongoTemplate mongoTemplate = taskMongoTemplateGetter.getTemplate(task);
        Criteria criteria = Criteria.where("status").in(statusList).and("actionId").is(actionId);
        return mongoTemplate.exists(Query.query(criteria), FetchUrlPo.class);
    }

    @Override
    public void createIndex(SpiderTaskPo task, String[] propArray, boolean unique) {
        MongoTemplate mongoTemplate = taskMongoTemplateGetter.getTemplate(task);
        Index indexDefinition = new Index();
        if (unique) {
            indexDefinition.unique();
        }

        for (String prop : propArray) {
            indexDefinition.on(prop, Sort.Direction.ASC);
        }

        mongoTemplate.indexOps(FetchUrlPo.class).ensureIndex(indexDefinition);
    }
}
