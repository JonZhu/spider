package com.zhujun.spider.master.domain;

/**
 * 分页
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public interface Paging extends DslAction {

	/**
	 * element选择器
	 */
	String getSelect();
	
	/**
	 * url属性
	 */
	String getUrlAttr();
	
}
