package com.zhujun.spider.master.schedule;

/**
 * Schedule工具
 * 
 * @author zhujun
 * @date 2016年6月7日
 *
 */
public class ScheduleUtil {

	public static String obj2str(Object obj) {
		return obj instanceof byte[] ? new String((byte[])obj) : String.valueOf(obj);
	}
	
}
