package com.zhujun.spider.master.domain;

/**
 * 数据采集定义
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public interface Spider extends DslParentAction {

	String getAuthor();
	
	String getDataDir();
	
}
