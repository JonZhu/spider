package com.zhujun.spider.master.schedule;

/**
 * 调度常量
 * 
 * @author zhujun
 * @date 2016年6月4日
 *
 */
public interface ScheduleConst {

	/**
	 * Data Scope中的spider taskId key
	 */
	String TASK_ID_KEY = "spider.run.id";
	
	/**
	 * Data Scope中的写数据 key
	 */
	String DATA_WRITER_KEY = "spider.datawriter";
	
	/**
	 * 前置结果数据
	 */
	String PRE_RESULT_DATA_KEY = "spider.preresult.data";
	
	/**
	 * 前置结果url
	 */
	String PRE_RESULT_URL_KEY = "spider.preresult.url";
	
	/**
	 * enum值分隔符
	 */
	String ENUM_VALUE_SEPARATOR = ";";
	
	/**
	 * 回车换行
	 */
	String CRNL = "\r\n";
	
}
