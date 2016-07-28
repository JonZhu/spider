package com.zhujun.spider.worker.mina;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.net.SpiderNetMessage;

public class ClientHandler implements IoHandler {

	private final static Logger LOG = LoggerFactory.getLogger(ClientHandler.class);
	
	private final Map<String, Queue<WaitMsgLock>> waitMsgQueueMap = new ConcurrentHashMap<>();
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		LOG.debug("sessionOpened");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		LOG.error("exceptionCaught", cause);

	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (message instanceof SpiderNetMessage) {
			SpiderNetMessage netMsg = (SpiderNetMessage)message;
			String action = netMsg.getHeader("Action");
			
			// 先处理等待锁逻辑
			Queue<WaitMsgLock> waitQueue = waitMsgQueueMap.get(action);
			boolean existWaitLock = false;
			if (waitQueue != null) {
				WaitMsgLock lock = waitQueue.poll();
				if (lock != null) {
					synchronized (lock) {
						lock.msg = netMsg; // 设置响应msg
						lock.notifyAll(); // 解除其它线程等待
					}
					
					existWaitLock = true;
				}
			}
			
			if (!existWaitLock) {
				// 无等待锁, 表示master主动推的消息
				nonWaitMsgReceived(session, netMsg);
			}
		}

	}

	/**
	 * 处理 无其它线程等待的, 服务器主动推送的消息
	 * 
	 * @author zhujun
	 * @date 2016年7月5日
	 *
	 * @param session
	 * @param netMsg
	 */
	protected void nonWaitMsgReceived(IoSession session, SpiderNetMessage netMsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 增加消息等待
	 * 
	 * @author zhujun
	 * @date 2016年7月5日
	 *
	 * @param msgAction
	 * @param lock
	 */
	public void addWaitMsgLock(String msgAction, WaitMsgLock lock) {
		
		Queue<WaitMsgLock> queue = waitMsgQueueMap.get(msgAction);
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<>();
			waitMsgQueueMap.put(msgAction, queue);
		}
		
		queue.add(lock);
	}

}
