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

import java.util.Date;
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
			IFetchResult result = null;
			try {
				result = CONTENT_FETCHER.fetch(item.url);
				// 复制http response header
				copyHttpResponseHeader(result.getHeaders(), netMsg);
				netMsg.setFetchResult(true); // 设置抓取成功标识
				netMsg.setStatusCode(result.getHttpStatusCode());
				netMsg.setBody(result.getData());
			} catch (Exception e) {
				LOG.error("fetch url[{}] data fail", item.url, e);
			}

			// 设置Spider头
			netMsg.setMsgType("Push-fetch-data");
			netMsg.setFetchUrl(item.url);
			netMsg.setFetchTime(new Date(System.currentTimeMillis()));
			netMsg.setTaskId(item.taskId);
			netMsg.setActionId(item.actionId);
			netMsg.setUrlId(item.id);
			
			pushData2master(netMsg);
		}
	}

	/**
	 * 复制http response header
	 * @param httpHeaders
	 * @param netMsg
	 */
	private void copyHttpResponseHeader(Map<String, String> httpHeaders, SpiderNetMessage netMsg) {
		if (httpHeaders == null || httpHeaders.isEmpty()) {
			return;
		}

		for (Map.Entry<String, String> httpEntry : httpHeaders.entrySet()) {
			if (SpiderNetMessage.HEADER_BODY_LENGTH.equals(httpEntry.getKey())) {
				// 排除该header，这个header由NetMessageEncoder添加
				continue;
			}
			netMsg.setHeader(httpEntry.getKey(), httpEntry.getValue());
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
