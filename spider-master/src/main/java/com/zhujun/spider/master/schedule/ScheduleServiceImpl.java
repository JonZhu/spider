package com.zhujun.spider.master.schedule;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.data.db.ISpiderTaskService;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.dsl.DslParser;
import com.zhujun.spider.master.dsl.XmlDslParserImpl;

@Singleton
public class ScheduleServiceImpl implements IScheduleService {

	private final static Logger LOG = LoggerFactory.getLogger(ScheduleServiceImpl.class);
	
	private final static AtomicInteger SCHEDULE_THREAD_INDEX = new AtomicInteger(0);
	
	private final Map<String, SpiderScheduleThread> spiderScheduleThreadMap = new ConcurrentHashMap<>();
	
	@Inject
	private ISpiderTaskService spiderTaskService;
	
	@Override
	public void startupAllSchedule() {
		LOG.debug("开始启动所有spider任务");
		List<SpiderTaskPo> taskList = null;
		try {
			taskList = spiderTaskService.findAllSpiderTask();
		} catch (Exception e) {
			throw new RuntimeException("无法启动, 查询任务失败", e);
		}
		
		if (taskList == null || taskList.isEmpty()) {
			return;
		}
		
		final DslParser dslParser = new XmlDslParserImpl(); // xml dsl解析
		
		for (SpiderTaskPo spiderTaskPo : taskList) {
			FileInputStream dslInputStream = null;
			try {
				File dslFile = new File(spiderTaskPo.getDatadir(), "spider.db");
				dslInputStream = new FileInputStream(dslFile);
				Spider spider = dslParser.parse(dslInputStream);
				startSchedule(spiderTaskPo.getId(), spider);
			} catch (Exception e) {
				LOG.error("启动任务失败, name:{}, datadir:{}", spiderTaskPo.getName(), spiderTaskPo.getDatadir(), e);
			} finally {
				IOUtils.closeQuietly(dslInputStream);
			}
		}
		
	}

	@Override
	public void startSchedule(String id, Spider spider) {
		SpiderScheduleThread thread = new SpiderScheduleThread(spider);
		thread.setName("SpiderScheduler-" + SCHEDULE_THREAD_INDEX.getAndIncrement());
		thread.start();
		spiderScheduleThreadMap.put(id, thread);
	}

	@Override
	public void stopSchedule(String id) {
		SpiderScheduleThread thread = spiderScheduleThreadMap.get(id);
		if (thread != null) {
			thread.stop();
			spiderScheduleThreadMap.remove(id);
		}
	}

}
