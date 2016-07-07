package com.zhujun.spider.master.schedule;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.data.writer.FileDataWriterImpl;
import com.zhujun.spider.master.data.writer.SpiderDataWriter;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.internal.XmlSpider;

public class SpiderScheduleThread extends Thread {

	private final static Logger LOG = LoggerFactory.getLogger(SpiderScheduleThread.class);
	
	private String taskId;
	private Spider spider;
	
	public SpiderScheduleThread(String taskId, Spider spider) {
		super();
		if (taskId == null) {
			throw new NullPointerException("taskId不能为空");
		}
		if (spider == null) {
			throw new NullPointerException("spider不能为空");
		}
		this.taskId = taskId;
		this.spider = spider;
		if (spider instanceof XmlSpider) {
			((XmlSpider) spider).setId(taskId);
		}
	}

	@Override
	public void run() {
		LOG.debug("开始执行spider [{}]", spider.getId());
		
		SpiderActionExecutor executor = new SpiderActionExecutor();
		Map<String, Serializable> dataScope = new HashMap<>(); // 数据域, 在执行过程中, 该数据会被持久化, 用于任务下次继续执行
		dataScope.put(ScheduleConst.TASK_ID_KEY, taskId); // 分配运行id
		
		// 构建数据存储写入器
		File dataDir = new File(spider.getDataDir());
		if (!dataDir.exists() || !dataDir.isDirectory()) {
			dataDir.mkdirs();
		}
		SpiderDataWriter dataWriter = new FileDataWriterImpl(new File(dataDir, "data").getAbsolutePath());
		
		// 初始化context
		ScheduleContextImpl context = new ScheduleContextImpl();
		context.setSpider(spider);
		context.setAction(spider);
		context.setDataWriter(dataWriter);
		context.setDataScope(dataScope);
		
		try {
			executor.execute(context);
		} catch (Exception e) {
			LOG.error("任务执行出错, name:{}, datadir:{}", spider.getId(), spider.getDataDir(), e);
		}
	}
	
	
	public Spider getSpider() {
		return this.spider;
	}
	
	private static class SpiderActionExecutor extends ParentActionExecutor implements ActionExecutor {

	}
	

}
