package com.zhujun.spider.worker;

import java.util.LinkedList;
import java.util.Queue;

import com.zhujun.spider.net.msgbody.PushUrlBodyItem;

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
	public final static Queue<PushUrlBodyItem> DATA = new LinkedList<>();
	
	private FetchUrlQueue(){};
}
