package com.zhujun.spider.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhujun.spider.net.mina.SpiderNetMessage;
import com.zhujun.spider.net.mina.msgbody.PushUrlBody;
import com.zhujun.spider.net.mina.msgbody.PushUrlBodyItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * 从master拉取url任务
 * 
 * @author zhujun
 * @date 2016年6月22日
 *
 */
public class PullUrlThread extends Thread {

	private final static Logger LOG = LoggerFactory.getLogger(PullUrlThread.class);
	
	private final MasterClient minaClient;
	
	/**
	 * 连续空响应次数
	 */
	private int responseEmptyCount = 0;
	
	public PullUrlThread(MasterClient minaClient) {
		this.minaClient = minaClient;
	}
	
	@Override
	public void run() {
		final BlockingQueue<PushUrlBodyItem> queue = FetchUrlQueue.DATA;
		
		while (!Thread.currentThread().isInterrupted()) {
			if (queue.size() > 100) {
				threadSleep(300);
				continue;
			}
			
			SpiderNetMessage netMsg = new SpiderNetMessage();
			netMsg.setHeader("Action", "Pull-url");
			SpiderNetMessage pushUrlMsg = minaClient.sendMsg(netMsg, 5000);
			
			int needSleep = 10000;
			if (pushUrlMsg != null) {
				String status = pushUrlMsg.getHeader("Status");
				if ("200".equals(status)) {
					// 正常响应
					
					String taskId = pushUrlMsg.getHeader("Task-id");
					try {
						PushUrlBody body = new ObjectMapper().readValue(pushUrlMsg.getBody(), PushUrlBody.class);
						if (body != null && !body.isEmpty()) {
							addUrlData2queue(taskId, body, queue);
							needSleep = 0; // 正常逻辑, 不用sleep
							responseEmptyCount = 0; // 重置空响应次数
						} else {
							// 响应数据为空
							responseEmptyCount++;
							if (responseEmptyCount < 3) {
								responseEmptyCount = 0; // 前两次不用sleep
							}
						}
					} catch (Exception e) {
						LOG.error("解析json出错", e);
					}
					
				}
			} else {
				needSleep = 0; // 超时不用sleep, 已经有timeout时间等待
			}
			
			if (needSleep > 0) {
				threadSleep(needSleep);
			}
			
		}
	}

	private void threadSleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void addUrlData2queue(String taskId, PushUrlBody body, BlockingQueue<PushUrlBodyItem> queue) {
		if (body == null || body.isEmpty()) {
			return;
		}
		
		for (PushUrlBodyItem item : body) {
			item.taskId = taskId;
			queue.add(item);
		}
		
		LOG.debug("add {} count of data to fetch url queue", body.size());
		
	}
	
}
