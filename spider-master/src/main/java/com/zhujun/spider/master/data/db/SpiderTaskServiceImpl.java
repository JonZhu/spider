package com.zhujun.spider.master.data.db;

import javax.inject.Singleton;

import org.apache.commons.dbutils.QueryRunner;

import com.zhujun.spider.master.domain.Spider;

@Singleton
public class SpiderTaskServiceImpl implements ISpiderTaskService {

	private final static QueryRunner QUERY_RUNNER = new QueryRunner();
	
	@Override
	public void createSpiderTask(Spider spider) {
		// TODO Auto-generated method stub

		// 校验是否已有datadir相同的任务
		
		
	}

	@Override
	public void findSpiderTaskList(int pageNo, int pageSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteSpiderTask(String taskId) {
		// TODO Auto-generated method stub

	}

}
