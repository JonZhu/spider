package com.zhujun.spider.master.schedule;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.data.writer.FileDataWriterImpl;
import com.zhujun.spider.master.data.writer.SpiderDataWriter;
import com.zhujun.spider.master.domain.Spider;

public class SpiderScheduleThread extends Thread {

	private final static Logger LOG = LoggerFactory.getLogger(SpiderScheduleThread.class);
	
	private Spider spider;
	
	public SpiderScheduleThread(Spider spider) {
		super();
		
		if (spider == null) {
			throw new NullPointerException("spider不能为空");
		}
		this.spider = spider;
	}

	@Override
	public void run() {
		LOG.debug("开始执行spider [{}]", spider.getName());
		
		SpiderActionExecutor executor = new SpiderActionExecutor();
		Map<String, Object> dataScope = new HashMap<>(); // 初始化
		dataScope.put(ScheduleConst.RUN_ID_KEY, UUID.randomUUID().toString()); // 分配运行id
		
		// 构建数据存储写入器
		File dataDir = new File(spider.getDataDir());
		if (!dataDir.exists() || !dataDir.isDirectory()) {
			dataDir.mkdirs();
		}
		SpiderDataWriter dataWriter = new FileDataWriterImpl(new File(dataDir, "data").getAbsolutePath());
		dataScope.put(ScheduleConst.DATA_WRITER_KEY, dataWriter);
		
		try {
			executor.execute(spider, spider, dataScope);
		} catch (Exception e) {
			LOG.error("任务执行出错, name:{}, datadir:{}", spider.getName(), spider.getDataDir(), e);
		}
	}
	
	
	public Spider getSpider() {
		return this.spider;
	}
	
	private static class SpiderActionExecutor extends ParentActionExecutor implements ActionExecutor {

	}
	

}
