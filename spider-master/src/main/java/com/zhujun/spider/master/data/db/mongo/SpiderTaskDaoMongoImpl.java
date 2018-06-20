package com.zhujun.spider.master.data.db.mongo;

import com.zhujun.spider.master.data.db.Page;
import com.zhujun.spider.master.data.db.dao.SpiderTaskDao;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhujun
 * @desc SpiderTaskDaoMongoImpl
 * @time 2018/6/19 11:27
 */
@Repository
public class SpiderTaskDaoMongoImpl implements SpiderTaskDao {
    private final static String COLLECTION_NAME = "spider_task";

    @Autowired
    private MongoTemplate masterMongoTemplate;

    @Override
    public int countByDatadir(String dataDir) {
        return (int)masterMongoTemplate.count(Query.query(Criteria.where("datadir").is(dataDir)), COLLECTION_NAME);
    }

    @Override
    public void insertSpiderTaskPo(SpiderTaskPo taskPo) {
        masterMongoTemplate.insert(taskPo);
    }

    @Override
    public Page<SpiderTaskPo> pagingTask(int pageNo, int pageSize) {
        Page<SpiderTaskPo> page = new Page<>();
        long count = masterMongoTemplate.getCollection(COLLECTION_NAME).count();
        page.setDataTotal((int)count);
        if (count > 0) {
            List<SpiderTaskPo> data = masterMongoTemplate.find(new Query().skip((pageNo - 1) * pageSize).limit(pageSize), SpiderTaskPo.class);
            page.setPageData(data);
        }

        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        page.setPageTotal(Page.calculatePageTotal((int)count, pageSize));

        return page;
    }

    @Override
    public void deleteTask(String taskId) {
        masterMongoTemplate.remove(Query.query(Criteria.where("_id").is(taskId)), COLLECTION_NAME);
    }

    @Override
    public List<SpiderTaskPo> findScheduledTask() {
        return masterMongoTemplate.find(Query.query(Criteria.where("status").is(SpiderTaskPo.Status.RUN)), SpiderTaskPo.class);
    }

    @Override
    public SpiderTaskPo getTaskById(String taskId) {
        return masterMongoTemplate.findOne(Query.query(Criteria.where("_id").is(taskId)), SpiderTaskPo.class);
    }

    @Override
    public int updateTaskStatus(String taskId, int status) {
        return (int)masterMongoTemplate.updateMulti(Query.query(Criteria.where("_id").is(taskId)),
                Update.update("status", status), COLLECTION_NAME).getModifiedCount();
    }
}
