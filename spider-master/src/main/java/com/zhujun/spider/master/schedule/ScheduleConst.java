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
	String TASK_ID_KEY = "spider.task.id";
	
	String TASK_DATA_DIR_KEY = "spider.task.datadir";
	
	/**
	 * 前置结果数据
	 */
	String PRE_RESULT_DATA_KEY = "spider.preresult.data";
	
	/**
	 * 前置结果url
	 */
	String PRE_RESULT_URL_KEY = "spider.preresult.url";
	
	/**
	 * 数据域持久化名称
	 */
	String DATA_SCOPE_PERSISENT_NAME_KEY = "spider.datascope.persistent.name";
	
	/**
	 * 执行进度, 如 0, 1, 1:0, 1:1, 2, 3
	 */
	String PROGRESS_KEY = "spider.progress";
	
	String HISTORY_PROGRESS_KEY = "spider.progress.history";
	
	/**
	 * enum值分隔符
	 */
	String ENUM_VALUE_SEPARATOR = ";";
	
	/**
	 * 回车换行
	 */
	String CRNL = "\r\n";

	
}
