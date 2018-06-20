package com.zhujun.spider.master.data.db.mongo;

import com.mongodb.MongoClient;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务隔离的mongo template
 *
 * @author zhujun
 * @desc TaskMongoTemplateGetter
 * @time 2018/6/19 16:38
 */
@Component
public class TaskMongoTemplateGetter {
    private final static Map<String, MongoTemplate> TEMPLATE_MAP = new HashMap<>();

    @Autowired
    private MongoClient mongoClient;

    public MongoTemplate getTemplate(SpiderTaskPo taskPo) {
        String mongoDb = "spider_task_" + taskPo.getId();
        MongoTemplate template = TEMPLATE_MAP.get(mongoDb);
        if (template == null) {
            synchronized (TEMPLATE_MAP) {
                template = TEMPLATE_MAP.get(mongoDb);
                if (template == null) { // 二次判断
                    // 创建
                    template = new MongoTemplate(mongoClient, mongoDb);
                    TEMPLATE_MAP.put(mongoDb, template);
                }
            }
        }

        return template;
    }

}
