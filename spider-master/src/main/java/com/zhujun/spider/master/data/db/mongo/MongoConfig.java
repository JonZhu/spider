package com.zhujun.spider.master.data.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Properties;

/**
 * @author zhujun
 * @desc MongoConfig
 * @time 2018/6/19 11:28
 */
@Configuration
public class MongoConfig {

    @Bean(name = "mongoMasterTemplate")
    public MongoTemplate getMasterMongoTemplate() {
        return new MongoTemplate(getMongoClient(), "spider_master");
    }

    @Bean
    public MongoClient getMongoClient() {
        Properties props = getMongoProps();
        MongoClientURI uri = new MongoClientURI(props.getProperty("url"));
        MongoClient mongoClient = new MongoClient(uri);
        return mongoClient;
    }

    @Bean
    @ConfigurationProperties(prefix = "spider.mongo")
    public Properties getMongoProps() {
        return new Properties();
    }

}
