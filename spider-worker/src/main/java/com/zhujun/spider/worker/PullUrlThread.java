package com.zhujun.spider.worker;

import java.util.Queue;

import com.zhujun.spider.net.SpiderNetMessage;
import com.zhujun.spider.worker.mina.MinaClient;

/**
 * 从master拉取url任务
 * 
 * @author zhujun
 * @date 2016年6月22日
 *
 */
public class PullUrlThread extends Thread {

	private final MinaClient minaClient;
	
	public PullUrlThread(MinaClient minaClient) {
		this.minaClient = minaClient;
	}
	
	@Override
	public void run() {
		final Queue<String> queue = FetchUrlQueue.DATA;
		
		while (!Thread.currentThread().isInterrupted()) {
			synchronized (queue) {
				if (queue.size() > 100) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			SpiderNetMessage netMsg = new SpiderNetMessage();
			netMsg.setHeader("Action", "Pull-url");
			minaClient.sendMsg(netMsg);
			SpiderNetMessage pushUrlMsg = minaClient.waitMsg("Push-url", 5000);
			if (pushUrlMsg != null) {
				
			}
			
		}
	}
	
}
