package com.zhujun.spider.worker;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * url抓取任务 全局队列
 * 
 * @author zhujun
 * @date 2016年6月22日
 *
 */
public class FetchUrlQueue {

	/**
	 * 队列数据
	 */
	public final static Queue<String> DATA = new ConcurrentLinkedQueue<>();
	
	private FetchUrlQueue(){};
}
