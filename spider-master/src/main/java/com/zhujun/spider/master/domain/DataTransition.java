package com.zhujun.spider.master.domain;

/**
 * 数据转换
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public interface DataTransition extends DslAction {

	/**
	 * 数据输入 action
	 */
	String getInput();
	
	/**
	 * element选择器
	 *
	 */
	String getSelect();
	
	/**
	 * element属性
	 */
	String getAttr();
	
	/**
	 * 数据正则表达式，取group(1)
	 */
	String getRegex();
	
}
