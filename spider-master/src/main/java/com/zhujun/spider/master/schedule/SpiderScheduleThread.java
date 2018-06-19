package com.zhujun.spider.master.schedule;

import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.data.writer.AppendFileDataWriterImpl;
import com.zhujun.spider.master.data.writer.EachFileDataWriterImpl;
import com.zhujun.spider.master.data.writer.SpiderDataWriter;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.internal.XmlSpider;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SpiderScheduleThread extends Thread {

	private final static Logger LOG = LoggerFactory.getLogger(SpiderScheduleThread.class);
	
	private SpiderTaskPo spiderTaskPo;
	private Spider spider;
	
	public SpiderScheduleThread(SpiderTaskPo spiderTaskPo, Spider spider) {
		super();
		if (spiderTaskPo == null) {
			throw new NullPointerException("spiderTaskPo不能为空");
		}
		if (spider == null) {
			throw new NullPointerException("spider不能为空");
		}
		this.spiderTaskPo = spiderTaskPo;
		this.spider = spider;
		if (spider instanceof XmlSpider) {
			((XmlSpider) spider).setId(spiderTaskPo.getId());
		}
	}

	@Override
	public void run() {
		LOG.debug("start schedule spider task, datadir: {}", spider.getDataDir());
		
		final String dataScopePersisentName = "datascope.bin"; // data域持久化文件名
		
		File dataScopeFile = new File(spider.getDataDir(), dataScopePersisentName);
		Map<String, Serializable> dataScope = loadDataScope(dataScopeFile);
		if (dataScope == null) {
			dataScope = new HashMap<>(); // 数据域, 在执行过程中, 该数据会被持久化, 用于任务下次继续执行
			dataScope.put(ScheduleConst.DATA_SCOPE_PERSISENT_NAME_KEY, dataScopePersisentName);
		} else {
			// 设置history progress
			setHistoryProgress(dataScope);
		}
		dataScope.put(ScheduleConst.TASK_ID_KEY, spiderTaskPo.getId()); // 任务id
		dataScope.put(ScheduleConst.TASK_DATA_DIR_KEY, spider.getDataDir());
		
		// 构建数据存储写入器
		SpiderDataWriter dataWriter = null;
		if ("eachfile".equalsIgnoreCase(spider.getDataWriterType())) {
			dataWriter = new EachFileDataWriterImpl(spider.getDataDir());
		} else {
			dataWriter = new AppendFileDataWriterImpl(new File(spider.getDataDir(), "data").getAbsolutePath());
		}
		
		// 初始化context
		ScheduleContextImpl context = new ScheduleContextImpl();
		context.setSpider(spider);
		context.setAction(spider);
		context.setDataWriter(dataWriter);
		context.setDataScope(dataScope);
		
		try {
			new SpiderActionExecutor().execute(context);
		} catch (Exception e) {
			LOG.error("schedule task error, datadir:{}", spider.getDataDir(), e);
		}
		
		LOG.debug("complete schedule spider task, datadir: {}", spider.getDataDir());
	}
	
	
	private void setHistoryProgress(Map<String, Serializable> dataScope) {
		if (!dataScope.containsKey(ScheduleConst.HISTORY_PROGRESS_KEY)) {
			String progress = (String)dataScope.get(ScheduleConst.PROGRESS_KEY);
			if (progress != null) {
				dataScope.put(ScheduleConst.HISTORY_PROGRESS_KEY, progress);
			}
		}
		
		// 总是清除progress
		dataScope.remove(ScheduleConst.PROGRESS_KEY);
	}

	/**
	 * 加载 dataScope数据, 不存在返回null
	 * @author zhujun
	 * @date 2016年7月7日
	 *
	 * @param dataScopeFile
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private Map<String, Serializable> loadDataScope(File dataScopeFile) {
		if (!dataScopeFile.exists()) {
			return null;
		}
		
		ObjectInputStream ois = null;
		try {
			
			ois = new ObjectInputStream(new FileInputStream(dataScopeFile));
			return (Map<String, Serializable>)ois.readObject();
		} catch (Exception e) {
			LOG.error("加载data scope出错", e);
		} finally {
			IOUtils.closeQuietly(ois);
		}
		
		return null;
	}

	public Spider getSpider() {
		return this.spider;
	}

	public SpiderTaskPo getSpiderTaskPo() {
		return spiderTaskPo;
	}

	private static class SpiderActionExecutor extends ParentActionExecutor implements ActionExecutor {

	}
	

}
