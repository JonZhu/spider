package com.zhujun.spider.master.data.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MongoConfigTest {

    @Autowired
    private MongoClient mongoClient;

    @Test
    public void getMongoClient() {
        MongoCollection<Document> collection = mongoClient.getDatabase("extract_xzqh").getCollection("test");
        Map data = new HashMap<>();
        data.put("_id", "5b3c7412d318841e24156b81");
        data.put("p2", 1111);
        data.put("p8", "888");
        saveData2Mongo(collection, data);
    }

    private final static UpdateOptions UPSERT_OPTIONS = new UpdateOptions().upsert(true);
    private static void saveData2Mongo(MongoCollection<org.bson.Document> baikeCollection, Object extractResult) {
        if (extractResult == null || !(extractResult instanceof Map)) {
            return;
        }

        org.bson.Document doc = new org.bson.Document((Map<String, Object>) extractResult);
        Object id = doc.get("_id");
        if (id != null) {
            // update
            baikeCollection.updateOne(Filters.eq(id), doc, UPSERT_OPTIONS);
        } else {
            // insert
            baikeCollection.insertOne(doc);
        }
    }
}