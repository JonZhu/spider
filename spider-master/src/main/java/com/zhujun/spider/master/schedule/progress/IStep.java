package com.zhujun.spider.master.schedule.progress;

import com.zhujun.spider.master.schedule.IScheduleContext;

/**
 * action内部 执行步骤
 * 
 * @author zhujun
 * @date 2016年7月11日
 *
 */
public interface IStep {
	void execute(IScheduleContext context) throws Exception;
}
