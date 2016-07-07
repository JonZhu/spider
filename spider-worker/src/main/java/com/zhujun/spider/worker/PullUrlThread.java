package com.zhujun.spider.worker;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhujun.spider.net.SpiderNetMessage;
import com.zhujun.spider.net.msgbody.PushUrlBody;
import com.zhujun.spider.net.msgbody.PushUrlBodyItem;
import com.zhujun.spider.worker.mina.MinaClient;

/**
 * 从master拉取url任务
 * 
 * @author zhujun
 * @date 2016年6月22日
 *
 */
public class PullUrlThread extends Thread {

	private final static Logger LOG = LoggerFactory.getLogger(PullUrlThread.class);
	
	private final MinaClient minaClient;
	
	public PullUrlThread(MinaClient minaClient) {
		this.minaClient = minaClient;
	}
	
	@Override
	public void run() {
		final Queue<PushUrlBodyItem> queue = FetchUrlQueue.DATA;
		
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
			
			boolean needSleep = true;
			if (pushUrlMsg != null) {
				System.out.println(pushUrlMsg);
				String status = pushUrlMsg.getHeader("Status");
				if ("200".equals(status)) {
					// 正常响应
					
					String taskId = pushUrlMsg.getHeader("Task-id");
					try {
						PushUrlBody body = new ObjectMapper().readValue(pushUrlMsg.getBody(), PushUrlBody.class);
						addUrlData2queue(taskId, body, queue);
						needSleep = false; // 正常逻辑, 不用sleep
					} catch (Exception e) {
						LOG.error("解析json出错", e);
					}
					
				}
			} else {
				needSleep = false; // 超时不用sleep, 已经有timeout时间等待
			}
			
			if (needSleep) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}

	private void addUrlData2queue(String taskId, PushUrlBody body, Queue<PushUrlBodyItem> queue) {
		if (body == null || body.isEmpty()) {
			return;
		}
		
		synchronized (queue) {
			for (PushUrlBodyItem item : body) {
				item.taskId = taskId;
				queue.add(item);
			}
			
			queue.notifyAll(); // 有新数据, 激活消费者
		}
		
	}
	
}
