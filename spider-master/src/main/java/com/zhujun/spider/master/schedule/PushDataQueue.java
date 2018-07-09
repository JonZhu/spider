package com.zhujun.spider.master.schedule;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * worker上传数据队列
 * 
 * @author zhujun
 * @date 2016年7月8日
 *
 */
public class PushDataQueue {

	private static final Map<String, Queue<Item>> QUEUE_MAP = new HashMap<>();
	
	/**
	 * 添加数据到队列
	 * 
	 * @author zhujun
	 * @date 2016年7月8日
	 *
	 * @param taskId
	 * @param actionId
	 * @param item
	 */
	public static void addPushData(String taskId, String actionId, Item item) {
		String queueKey = getQueueKey(taskId, actionId);
		Queue<Item> queue = QUEUE_MAP.get(queueKey);
		if (queue == null) {
			synchronized (QUEUE_MAP) {
				queue = QUEUE_MAP.get(queueKey);
				if (queue == null) {
					queue = new ConcurrentLinkedQueue<>();
					QUEUE_MAP.put(queueKey, queue);
				}
			}
		}

		queue.add(item);
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
	public static Item popPushData(String taskId, String actionId) {
		Queue<Item> queue = QUEUE_MAP.get(getQueueKey(taskId, actionId));
		return queue == null ? null : queue.poll();
	}

	/**
	 * 获取队列中task的数据
	 * @param taskId
	 * @return
	 */
	public static int getDataCount(String taskId) {
		int count = 0;
		Set<Map.Entry<String, Queue<Item>>> entrySet = QUEUE_MAP.entrySet();
		for (Map.Entry<String, Queue<Item>> entry : entrySet) {
			if (entry.getKey().startsWith(taskId)) {
				count += entry.getValue().size();
			}
		}
		return count;
	}


	public static class Item {
		public String taskId;
		public String actionId;
		public String urlId;
		public String url;
		public byte[] data;
		public Date fetchTime;
		public boolean success;
		public String contentType;
		public Integer httpStatusCode;
	}
	
}
