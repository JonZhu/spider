package com.zhujun.spider.master.schedule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.domain.Spider;

public class SpiderScheduleImpl implements SpiderSchedule {

	private final static Logger LOG = LoggerFactory.getLogger(SpiderScheduleImpl.class);
	
	private Spider spider;
	
	public SpiderScheduleImpl(Spider spider) {
		if (spider == null) {
			throw new NullPointerException("spider不能为空");
		}
		this.spider = spider;
	}

	@Override
	public void start() {
		LOG.debug("开始执行spider [{}]", spider.getName());
		
		SpiderActionExecutor executor = new SpiderActionExecutor();
		Map<String, Object> dataScope = new HashMap<>(); // 初始化
		dataScope.put(ScheduleConst.RUN_ID_KEY, UUID.randomUUID().toString()); // 分配运行id
		executor.execute(spider, spider, dataScope);
	}
	
	
	private static class SpiderActionExecutor extends ParentActionExecutor implements ActionExecutor {

	}
	

}
