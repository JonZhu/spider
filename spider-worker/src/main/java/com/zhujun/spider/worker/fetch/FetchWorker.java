package com.zhujun.spider.worker.fetch;

import com.zhujun.spider.net.mina.SpiderNetMessage;
import com.zhujun.spider.net.mina.msgbody.PushUrlBodyItem;
import com.zhujun.spider.net.url.ContentFetcher;
import com.zhujun.spider.net.url.IFetchResult;
import com.zhujun.spider.net.url.JavaUrlContentFetcher;
import com.zhujun.spider.worker.FetchUrlQueue;
import com.zhujun.spider.worker.MasterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 内容抓取线程
 * 
 * <p>从队列中获取抓取任务, 抓取内容, 上报数据给master</p>
 * 
 * @author zhujun
 * @date 2016年6月22日
 *
 */
public class FetchWorker implements Runnable {
	private final static Logger LOG = LoggerFactory.getLogger(FetchWorker.class);
	
	private final static ContentFetcher CONTENT_FETCHER = JavaUrlContentFetcher.getInstance();
	
	/**
	 * 同一个host，只允许一个线程抓取
	 */
	private final static Map<String, Lock> HOST_LOCK_MAP = new HashMap<>();

	private final MasterClient minaClient;
	
	public FetchWorker(MasterClient minaClient) {
		if (minaClient == null) {
			throw new NullPointerException("minaClient不能为空");
		}
		this.minaClient = minaClient;
	}
	
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			
			PushUrlBodyItem item = null;
			try {
				item = getUrlFromQueue();
			} catch (InterruptedException e1) {
				break;
			}
			SpiderNetMessage netMsg = new SpiderNetMessage();
			netMsg.setHeader("Action", "Push-fetch-data");
			netMsg.setHeader("Fetch-url", item.url);
			
			IFetchResult result = null;
			try {
				result = CONTENT_FETCHER.fetch(item.url);
				netMsg.setHeader("Fetch-Result", "Success");
				netMsg.setHeader("Content-Type", result.getContentType());
				netMsg.setBody(result.getData());
			} catch (Exception e) {
				LOG.error("fetch url[{}] data fail", item.url, e);
			}
			
			netMsg.setHeader("Fetch-time", String.valueOf(System.currentTimeMillis()));
			netMsg.setHeader("Task_id", item.taskId);
			netMsg.setHeader("Action_id", item.actionId);
			netMsg.setHeader("Url_id", String.valueOf(item.id));
			
			pushData2master(netMsg);
		}
	}


	private Lock getHostFetchLock(String host) {
		Lock lock = null;
		synchronized (HOST_LOCK_MAP) {
			lock = HOST_LOCK_MAP.get(host);
			if (lock == null) {
				lock = new ReentrantLock();
				HOST_LOCK_MAP.put(host, lock);
			}
			
		}
		
		lock.lock();
		return lock;
	}


	/**
	 * 上传数据给master
	 * 
	 * @author zhujun
	 * @date 2016年6月22日
	 *
	 * @param netMsg
	 */
	private void pushData2master(SpiderNetMessage netMsg) {
		minaClient.sendMsg(netMsg);
	}


	/**
	 * 从队列获取url, 如果队列empty, 则等待
	 * @author zhujun
	 * @date 2016年6月22日
	 *
	 * @return
	 * @throws InterruptedException 
	 */
	private PushUrlBodyItem getUrlFromQueue() throws InterruptedException {
		// queue is blocking
		return FetchUrlQueue.DATA.poll(999999, TimeUnit.DAYS);
	}

}
