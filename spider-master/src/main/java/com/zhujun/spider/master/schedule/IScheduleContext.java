package com.zhujun.spider.master.schedule;

import java.io.Serializable;
import java.util.Map;

import com.zhujun.spider.master.data.writer.SpiderDataWriter;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.Spider;

/**
 * spider调度 上下文环境
 * 
 * @author zhujun
 * @date 2016年7月7日
 *
 */
public interface IScheduleContext {

	/**
	 * 任务定义
	 * 
	 * @author zhujun
	 * @date 2016年7月7日
	 *
	 * @return
	 */
	Spider getSpider();
	
	/**
	 * 当前执行的action
	 * 
	 * @author zhujun
	 * @date 2016年7月7日
	 *
	 * @return
	 */
	DslAction getAction();
	
	void setAction(DslAction action);
	
	/**
	 * 数据域
	 * 
	 * @author zhujun
	 * @date 2016年7月7日
	 *
	 * @return
	 */
	Map<String, Serializable> getDataScope();
	
	SpiderDataWriter getDataWriter();
	
}
