package com.zhujun.spider.master.data.db.sqlite;

import static org.junit.Assert.*;

import org.junit.Test;

public class DataSourceManagerTest {

	@Test
	public void testRegist() {
		DataSourceManager.regist("./data/master.db", DataSourceType.Master);
		DataSourceManager.regist("./data/spider.db", DataSourceType.Spider);
	}

	@Test
	public void testGetDataSource() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemove() {
		fail("Not yet implemented");
	}

}
