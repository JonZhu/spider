package com.zhujun.spider.master.data.db.mongo;

import com.zhujun.spider.master.data.db.dao.HtmlExtractTaskDao;
import com.zhujun.spider.master.data.db.po.HtmlExtractTaskPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Administrator
 * @desc HtmlExtractTaskDaoMongoImpl
 * @time 2018/7/2 10:56
 */
@Repository
public class HtmlExtractTaskDaoMongoImpl implements HtmlExtractTaskDao {

    @Autowired
    private MongoTemplate masterMongoTemplate;

    public void insert(HtmlExtractTaskPo task) {
        masterMongoTemplate.insert(task);
    }

    public HtmlExtractTaskPo getById(String taskId) {
        return masterMongoTemplate.findOne(Query.query(Criteria.where("_id").is(taskId)), HtmlExtractTaskPo.class);
    }

    @Override
    public void updateStatus(String taskId, int status) {
        masterMongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(taskId)),
                Update.update("status", status), HtmlExtractTaskPo.class);
    }

    @Override
    public void setErrorInfo(String taskId, String errorInfo) {
        masterMongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(taskId)),
                Update.update("status", HtmlExtractTaskPo.Status.ERROR).set("errorInfo", errorInfo), HtmlExtractTaskPo.class);
    }

    @Override
    public void incrementSuccessCount(String taskId) {
        masterMongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(taskId)),
                new Update().inc("successCount", 1), HtmlExtractTaskPo.class);
    }

    public void incrementFailCount(String taskId) {
        masterMongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(taskId)),
                new Update().inc("failCount", 1), HtmlExtractTaskPo.class);
    }

    @Override
    public void saveExtractCount(String taskId, int successCount, int failCount) {
        masterMongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(taskId)),
                new Update().set("successCount", successCount).set("failCount", failCount), HtmlExtractTaskPo.class);
    }

    @Override
    public List<HtmlExtractTaskPo> findTask() {
        return masterMongoTemplate.find(Query.query(Criteria.where("status").ne(HtmlExtractTaskPo.Status.DELETED)),
                HtmlExtractTaskPo.class);
    }
}
