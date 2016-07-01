package com.zhujun.spider.master.schedule;

import com.zhujun.spider.master.domain.Spider;

/**
 * 调度服务, 调度系统与外部的接口
 * 
 * <p>主要管理调用线程, 提供调度器的增加、停止</p>
 * 
 * @author zhujun
 * @date 2016年7月1日
 *
 */
public interface IScheduleService {

	/**
	 * 系统初始化时，启动所有调度器
	 * @author zhujun
	 * @date 2016年7月1日
	 *
	 */
	void startupAllSchedule();
	
	/**
	 * 启动 spider
	 * @author zhujun
	 * @date 2016年7月1日
	 *
	 * @param id 唯一编号
	 * @param spider spider定义
	 */
	void startSchedule(String id, Spider spider);
	
	/**
	 * 停止 spider
	 * @author zhujun
	 * @date 2016年7月1日
	 *
	 * @param id
	 */
	void stopSchedule(String id);
	
}
