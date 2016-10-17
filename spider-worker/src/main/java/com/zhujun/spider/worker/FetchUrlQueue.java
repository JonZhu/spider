package com.zhujun.spider.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.zhujun.spider.net.mina.msgbody.PushUrlBodyItem;

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
	public final static BlockingQueue<PushUrlBodyItem> DATA = new LinkedBlockingQueue<>();
	
	private FetchUrlQueue(){};
}
