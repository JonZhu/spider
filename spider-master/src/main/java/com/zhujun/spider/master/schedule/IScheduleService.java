package com.zhujun.spider.master.schedule;

import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.domain.Spider;
import org.apache.commons.lang3.tuple.Pair;

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
	 * @param spiderTaskPo
	 * @param spider spider定义
	 */
	void startSchedule(SpiderTaskPo spiderTaskPo, Spider spider);
	
	/**
	 * 停止 spider
	 * @author zhujun
	 * @date 2016年7月1日
	 *
	 * @param id
	 */
	void stopSchedule(String id);
	
	/**
	 * 暂停调度
	 * @author zhujun
	 * @date 2016年8月4日
	 *
	 * @param id
	 */
	void pauseSchedule(String id);
	
	/**
	 * 恢复调度
	 * @author zhujun
	 * @date 2016年8月4日
	 *
	 * @param spiderTaskPo
	 * @param spider
	 */
	void resumeSchedule(SpiderTaskPo spiderTaskPo, Spider spider);
	
	/**
	 * 随机获取一个正在执行的任务
	 * @return
	 */
	Pair<String, SpiderTaskPo> randomRunningScheduleTask();
	
}
