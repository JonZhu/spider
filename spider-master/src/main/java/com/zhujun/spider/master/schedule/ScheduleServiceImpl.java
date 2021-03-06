package com.zhujun.spider.master.schedule;

import com.zhujun.spider.master.data.db.ISpiderTaskService;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.dsl.DslParser;
import com.zhujun.spider.master.dsl.XmlDslParserImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ScheduleServiceImpl implements IScheduleService {

	private final static Logger LOG = LoggerFactory.getLogger(ScheduleServiceImpl.class);
	
	private final static AtomicInteger SCHEDULE_THREAD_INDEX = new AtomicInteger(0);
	
	/**
	 * 运行中的任务调度
	 */
	private final Map<String, SpiderScheduleThread> runningScheduleThreadMap = new ConcurrentHashMap<>();
	
	/**
	 * 暂停的任务调度
	 */
	private final Map<String, SpiderScheduleThread> pausedScheduleThreadMap = new ConcurrentHashMap<>();
	
	
	@Autowired
	private ISpiderTaskService spiderTaskService;
	
	@Override
	public void startupAllSchedule() {
		LOG.debug("start boot all spider task");
		List<SpiderTaskPo> taskList = null;
		try {
			taskList = spiderTaskService.findAllScheduleSpiderTask();
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
				File dslFile = new File(spiderTaskPo.getDatadir(), "spiderdsl.xml");
				dslInputStream = new FileInputStream(dslFile);
				Spider spider = dslParser.parse(dslInputStream);
				startSchedule(spiderTaskPo, spider);
			} catch (Exception e) {
				LOG.error("boot task fail, name:{}, datadir:{}", spiderTaskPo.getName(), spiderTaskPo.getDatadir(), e);
			} finally {
				IOUtils.closeQuietly(dslInputStream);
			}
		}
		
	}

	@Override
	public void startSchedule(SpiderTaskPo spiderTaskPo, Spider spider) {
		SpiderScheduleThread thread = new SpiderScheduleThread(spiderTaskPo, spider);
		thread.setName("SpiderScheduler-" + SCHEDULE_THREAD_INDEX.getAndIncrement());
		thread.start();
		runningScheduleThreadMap.put(spiderTaskPo.getId(), thread);
	}

	@Override
	public void stopSchedule(String id) {
		SpiderScheduleThread thread = runningScheduleThreadMap.remove(id);
		if (thread != null) {
			thread.interrupt();
		}

		thread = runningScheduleThreadMap.remove(id);
		if (thread != null) {
			thread.interrupt();
		}
	}
	
	/**
	 * 随机获取一个正在执行的任务id
	 * @return
	 */
	public Pair<String, SpiderTaskPo> randomRunningScheduleTask() {
		Set<String> keySet = runningScheduleThreadMap.keySet();
		if (keySet.isEmpty()) {
			return null;
		}
		Object[] arr = keySet.toArray();
		String taskId = (String)arr[new Random().nextInt(arr.length)];
		
		SpiderScheduleThread thread = runningScheduleThreadMap.get(taskId);
		return ImmutablePair.of(taskId, thread.getSpiderTaskPo());
	}

	@Override
	public void pauseSchedule(String id) {
		SpiderScheduleThread thread = runningScheduleThreadMap.remove(id);
		if (thread != null) {
			pausedScheduleThreadMap.put(id, thread);
		}
	}

	@Override
	public void resumeSchedule(SpiderTaskPo spiderTaskPo, Spider spider) {
		String id = spiderTaskPo.getId();
		SpiderScheduleThread thread = pausedScheduleThreadMap.remove(id);
		if (thread == null) {
			startSchedule(spiderTaskPo, spider);
		} else {
			runningScheduleThreadMap.put(id, thread);
		}
	}

}
