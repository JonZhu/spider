package com.zhujun.spider.master.domain;

/**
 * 数据采集定义
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public interface Spider extends DslParentAction {

	String getName();
	
	String getAuthor();
	
	String getDataDir();
	
	/**
	 * 写数据 类型
	 * 
	 * @return
	 */
	String getDataWriterType();
	
}
