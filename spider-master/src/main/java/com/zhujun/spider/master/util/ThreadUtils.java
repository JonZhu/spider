package com.zhujun.spider.master.util;

/**
 * 线程工具
 * 
 * @author zhujun
 * @date 2016年7月8日
 *
 */
public class ThreadUtils {

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
