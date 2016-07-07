package com.zhujun.spider.worker.fetch;

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
			
			byte[] content = null;
			try {
				content = CONTENT_FETCHER.fetch(item.url);
				netMsg.setHeader("Fetch-Result", "Success");
				netMsg.setBody(content);
			} catch (Exception e) {
				LOG.error("获取url[{}]数据失败", item.url, e);
			}
			
			netMsg.setHeader("Fetch-time", String.valueOf(System.currentTimeMillis()));
			netMsg.setHeader("Task_id", item.taskId);
			netMsg.setHeader("Action_id", item.actionId);
			netMsg.setHeader("Url_id", String.valueOf(item.id));
			
			pushData2master(netMsg);
		}
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
		
		synchronized (FetchUrlQueue.DATA) {
			if (FetchUrlQueue.DATA.isEmpty()) {
				try {
					FetchUrlQueue.DATA.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			url = FetchUrlQueue.DATA.poll();
			FetchUrlQueue.DATA.notifyAll();
		}
		
		return url;
	}

}
