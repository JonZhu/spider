package com.zhujun.spider.master.data.db;

import com.mongodb.MongoWriteException;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.exception.ExceptionIgnore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class FetchUrlServiceImplTest {

    @Autowired
    private FetchUrlServiceImpl fetchUrlService;

    @Test
    public void createFetchUrl() {
        String taskId = "94df61e720af4135a2f7292ca13a90ef";
        SpiderTaskPo task = new SpiderTaskPo();
        task.setId(taskId);
        FetchUrlPo fetchUrlPo = new FetchUrlPo();
        fetchUrlPo.setUrl("test" + new String(new byte[5000]) + "test"); // 测试url过长
        try {
            fetchUrlService.createFetchUrl(task, fetchUrlPo, new CreateFetchUrlExceptionIgnore());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static class CreateFetchUrlExceptionIgnore implements ExceptionIgnore {
        @Override
        public boolean isIgnore(Exception e) {
            Throwable throwable = e;
            while (true) {
                if (throwable == null) {
                    break;
                }
                if (throwable instanceof MongoWriteException) {
                    if (((MongoWriteException) throwable).getCode() == 17280) {
                        // com.mongodb.MongoWriteException: WiredTigerIndex::insert: key too large to index, failing  1047
                        return true;
                    }
                }

                throwable = throwable.getCause(); // 遍历cause链
            }

            return false;
        }
    }
}