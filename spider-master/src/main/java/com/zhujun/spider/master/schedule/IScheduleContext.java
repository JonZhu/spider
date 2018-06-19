package com.zhujun.spider.master.schedule;

import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.data.writer.SpiderDataWriter;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.Spider;

import java.io.Serializable;
import java.util.Map;

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

	SpiderTaskPo getSpiderTaskPo();
	
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
	 * pre action
	 * 
	 * @author zhujun
	 * @date 2016年7月14日
	 *
	 * @return
	 */
	DslAction getParentAction();
	
	void setParentAction(DslAction action);
	
	
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
