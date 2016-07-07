package com.zhujun.spider.master.schedule;

/**
 * Action执行
 * 
 * @author zhujun
 * @date 2016年6月4日
 *
 */
public interface ActionExecutor {

	/**
	 * 执行Action
	 * 
	 * @param spider 
	 * @param action 当前action
	 * @param dataScope 数据域
	 */
	void execute(IScheduleContext context) throws Exception;
	
}
