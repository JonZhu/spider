package com.zhujun.spider.master.schedule;

import java.util.Map;

import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.Spider;

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
	void execute(Spider spider, DslAction action, Map<String, Object> dataScope);
	
}
