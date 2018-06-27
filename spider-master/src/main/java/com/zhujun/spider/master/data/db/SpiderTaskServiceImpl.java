package com.zhujun.spider.master.data.db;

import com.zhujun.spider.master.data.db.dao.FetchUrlDao;
import com.zhujun.spider.master.data.db.dao.SpiderTaskDao;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo.Status;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.internal.XmlSpider;
import com.zhujun.spider.master.dsl.DslParser;
import com.zhujun.spider.master.dsl.XmlDslParserImpl;
import com.zhujun.spider.master.schedule.IScheduleService;
import com.zhujun.spider.master.util.UuidUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Date;
import java.util.List;

@Service
public class SpiderTaskServiceImpl implements ISpiderTaskService {
	private final static Logger LOG = LoggerFactory.getLogger(SpiderTaskServiceImpl.class);

	@Autowired
	private IScheduleService scheduleService;

	@Autowired
	private SpiderTaskDao spiderTaskDao;

	@Autowired
	private FetchUrlDao fetchUrlDao;
	
	@Override
	public void createSpiderTask(final Spider spider) throws Exception {

		// 校验是否已有datadir相同的任务
		if (spiderTaskDao.countByDatadir(spider.getDataDir()) > 0) {
			throw new Exception("datadir被其它任务占用");
		}

		SpiderTaskPo taskPo = new SpiderTaskPo();
		taskPo.setId(UuidUtil.create());
		taskPo.setName(spider.getName());
		taskPo.setAuthor(spider.getAuthor());
		taskPo.setDatadir(spider.getDataDir());
		taskPo.setCreateTime(new Date(System.currentTimeMillis()));
		taskPo.setStatus(Status.RUN);
		spiderTaskDao.insertSpiderTaskPo(taskPo);

		// spider 数据目录
		File spiderDataDir = new File(spider.getDataDir());
		if (!spiderDataDir.exists()) {
			spiderDataDir.mkdirs();
		}

		// 储存dsl
		if (spider instanceof XmlSpider) {
			XmlSpider xmlSpider = (XmlSpider)spider;
			File dslFile = new File(spiderDataDir, "spiderdsl.xml");
			Writer dslWriter = null;
			try {
				dslWriter = new OutputStreamWriter(new FileOutputStream(dslFile), "UTF-8");
				xmlSpider.getSpiderDslDoc().getRootElement().addAttribute("id", taskPo.getId());
				xmlSpider.getSpiderDslDoc().write(dslWriter);
			} finally {
				IOUtils.closeQuietly(dslWriter);
			}

		}

		// 创建fetchurl.url索引
		fetchUrlDao.createIndex(taskPo, new String[]{"url"}, false);
		// 创建fetchurl.status索引
		fetchUrlDao.createIndex(taskPo, new String[]{"status"}, false);

		// 启动调度
		scheduleService.startSchedule(taskPo, spider);
	}


	@Override
	public Page<SpiderTaskPo> findSpiderTaskList(final int pageNo, final int pageSize) throws Exception {
		return spiderTaskDao.pagingTask(pageNo, pageSize);
	}

	@Override
	public void deleteSpiderTask(final String taskId) throws Exception {
		spiderTaskDao.deleteTask(taskId);
		// 停止调度
		scheduleService.stopSchedule(taskId);
	}
	


	@Override
	public List<SpiderTaskPo> findAllScheduleSpiderTask() throws Exception {
		return spiderTaskDao.findScheduledTask();
	}

	@Override
	public void pauseTask(final String taskId) throws Exception {
		SpiderTaskPo task = spiderTaskDao.getTaskById(taskId);
		if (task == null) {
			throw new Exception("任务不存在");
		}

		if (task.getStatus() != Status.RUN) {
			throw new Exception("任务不能暂停");
		}

		scheduleService.pauseSchedule(taskId);

		spiderTaskDao.updateTaskStatus(taskId, Status.PAUSED);
	}

	@Override
	public void resumeTask(final String taskId) throws Exception {
		SpiderTaskPo task = spiderTaskDao.getTaskById(taskId);
		if (task == null) {
			throw new Exception("任务不存在");
		}

		if (task.getStatus() != Status.PAUSED) {
			throw new Exception("任务不能恢复");
		}

		// 加载spider dsl
		FileInputStream dslInputStream = null;
		Spider spider = null;

		try {
			final DslParser dslParser = new XmlDslParserImpl(); // xml dsl解析
			File dslFile = new File(task.getDatadir(), "spiderdsl.xml");
			dslInputStream = new FileInputStream(dslFile);
			spider = dslParser.parse(dslInputStream);
		} finally {
			IOUtils.closeQuietly(dslInputStream);
		}

		// 恢复调度
		scheduleService.resumeSchedule(task, spider);

		spiderTaskDao.updateTaskStatus(taskId, Status.RUN);
	}

	@Override
	public SpiderTaskPo getSpiderTask(final String taskId) {
		return spiderTaskDao.getTaskById(taskId);
	}

	@Override
	public void completeTask(String taskId, String errorInfo) {
		scheduleService.stopSchedule(taskId); // 停止调度
		if (errorInfo == null) {
			LOG.info("task {} success complete", taskId);
			spiderTaskDao.updateTaskStatus(taskId, Status.COMPLETE);
		} else {
			LOG.error("task {} error complete, message: \n{}", taskId, errorInfo);
			spiderTaskDao.updateTaskStatus(taskId, Status.ERROR);
		}
	}

}
