package com.zhujun.spider.master.schedule;

import com.zhujun.spider.net.mina.SpiderNetMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * worker上传数据队列
 * 
 * @author zhujun
 * @date 2016年7月8日
 *
 */
public class PushDataQueue {

	private static final Map<String, Queue<SpiderNetMessage>> QUEUE_MAP = new HashMap<>();
	
	/**
	 * 添加数据到队列
	 * 
	 * @author zhujun
	 * @date 2016年7月8日
	 *
	 * @param taskId
	 * @param actionId
	 * @param netMessage
	 */
	public static void addPushData(String taskId, String actionId, SpiderNetMessage netMessage) {
		String queueKey = getQueueKey(taskId, actionId);
		Queue<SpiderNetMessage> queue = QUEUE_MAP.get(queueKey);
		if (queue == null) {
			synchronized (QUEUE_MAP) {
				queue = QUEUE_MAP.get(queueKey);
				if (queue == null) {
					queue = new ConcurrentLinkedQueue<>();
					QUEUE_MAP.put(queueKey, queue);
				}
			}
		}

		queue.add(netMessage);
	}
	
	private static String getQueueKey(String taskId, String actionId) {
		return taskId + "/" + actionId;
	}

	/**
	 * 出队, 如果队列中无数据, 返回null
	 * 
	 * @author zhujun
	 * @date 2016年7月8日
	 *
	 * @param taskId
	 * @param actionId
	 * @return
	 */
	public static SpiderNetMessage popPushData(String taskId, String actionId) {
		Queue<SpiderNetMessage> queue = QUEUE_MAP.get(getQueueKey(taskId, actionId));
		return queue == null ? null : queue.poll();
	}

	/**
	 * 获取队列中task的数据
	 * @param taskId
	 * @return
	 */
	public static int getDataCount(String taskId) {
		int count = 0;
		Set<Map.Entry<String, Queue<SpiderNetMessage>>> entrySet = QUEUE_MAP.entrySet();
		for (Map.Entry<String, Queue<SpiderNetMessage>> entry : entrySet) {
			if (entry.getKey().startsWith(taskId)) {
				count += entry.getValue().size();
			}
		}
		return count;
	}

}
