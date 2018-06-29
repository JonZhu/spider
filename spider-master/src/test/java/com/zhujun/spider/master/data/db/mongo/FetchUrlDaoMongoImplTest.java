package com.zhujun.spider.master.data.db.mongo;

import com.zhujun.spider.master.data.db.dao.FetchUrlDao;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class FetchUrlDaoMongoImplTest {
    @Autowired
    private FetchUrlDao fetchUrlDao;

    @Test
    public void findFetchurl() throws ParseException {
        String taskId = "94df61e720af4135a2f7292ca13a90ef";
        Date date = DateUtils.parseDate("2018-06-21T15:31:41.643", "yyyy-MM-dd'T'HH:mm:ss.SSS");
        SpiderTaskPo task = new SpiderTaskPo();
        task.setId(taskId);
        List<FetchUrlPo> fetchUrlPoList = fetchUrlDao.findFetchurl(task, FetchUrlPo.STATUS_PUSHED, date, 10);
        for (FetchUrlPo url : fetchUrlPoList) {
            System.out.println(url.getUrl());
        }
    }
}