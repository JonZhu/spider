package com.zhujun.spider.master.schedule;

import java.io.Serializable;
import java.util.Map;

import com.zhujun.spider.master.data.writer.SpiderDataWriter;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.Spider;

/**
 * Schedule context实现
 * 
 * @author zhujun
 * @date 2016年7月7日
 *
 */
public class ScheduleContextImpl implements IScheduleContext {

	private Spider spider;
	private DslAction action;
	
	private Map<String, Serializable> dataScope;
	
	private SpiderDataWriter dataWriter;

	public Spider getSpider() {
		return spider;
	}

	public void setSpider(Spider spider) {
		this.spider = spider;
	}

	public DslAction getAction() {
		return action;
	}

	public void setAction(DslAction action) {
		this.action = action;
	}

	public Map<String, Serializable> getDataScope() {
		return dataScope;
	}

	public void setDataScope(Map<String, Serializable> dataScope) {
		this.dataScope = dataScope;
	}

	public SpiderDataWriter getDataWriter() {
		return dataWriter;
	}

	public void setDataWriter(SpiderDataWriter dataWriter) {
		this.dataWriter = dataWriter;
	}
	
}
