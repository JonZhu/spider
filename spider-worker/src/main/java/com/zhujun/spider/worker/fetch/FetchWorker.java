package com.zhujun.spider.worker.fetch;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.net.SpiderNetMessage;
import com.zhujun.spider.net.msgbody.PushUrlBodyItem;
import com.zhujun.spider.worker.FetchUrlQueue;
import com.zhujun.spider.worker.mina.MinaClient;

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

	private final MinaClient minaClient;
	
	public FetchWorker(MinaClient minaClient) {
		if (minaClient == null) {
			throw new NullPointerException("minaClient不能为空");
		}
		this.minaClient = minaClient;
	}
	
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			
			PushUrlBodyItem item = getUrlFromQueue();
			SpiderNetMessage netMsg = new SpiderNetMessage();
			netMsg.setHeader("Action", "Push-fetch-data");
			netMsg.setHeader("Fetch-url", item.url);
			
			IFetchResult result = null;
			Lock lock = null;
			try {
				
				URL url = new URL(item.url);
				lock = getHostFetchLock(url.getHost());
				
				result = CONTENT_FETCHER.fetch(item.url);
				netMsg.setHeader("Fetch-Result", "Success");
				netMsg.setHeader("Content-Type", result.getContentType());
				netMsg.setBody(result.getData());
			} catch (Exception e) {
				LOG.error("获取url[{}]数据失败", item.url, e);
			} finally {
				lock.unlock();
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
	 */
	private PushUrlBodyItem getUrlFromQueue() {
		PushUrlBodyItem url = null;
		Queue<PushUrlBodyItem> queue = FetchUrlQueue.DATA;
		
		while(true) {
			synchronized (queue) {
				if (queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				url = queue.poll();
				if (url == null) {
					continue;
				}
				
				if (queue.size() < 100) {
					queue.notifyAll(); // 通知push url
				}
				break;
			}
		}
		
		return url;
	}

}
