package com.zhujun.spider.worker.mina;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.net.mina.SpiderNetMessage;

public class ClientHandler implements IoHandler {

	private final static Logger LOG = LoggerFactory.getLogger(ClientHandler.class);
	
	private final Map<String, WaitMsgLock> waitMsgQueueMap = new ConcurrentHashMap<>();
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		LOG.debug("session {} Created", session.getId());

	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		LOG.debug("session {} Opened", session.getId());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		LOG.debug("session {} Closed", session.getId());
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		LOG.debug("session {} Idle:{}", session.getId(), status.toString());
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		LOG.error("session {} exceptionCaught", session.getId(), cause);
		if (cause instanceof IOException) {
		    LOG.debug("invoke session.closeNow()");
			session.closeNow();
		}
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (message instanceof SpiderNetMessage) {
			SpiderNetMessage netMsg = (SpiderNetMessage)message;
			String respForMsgId = netMsg.getHeader("Response-for");
			
			boolean existWaitLock = false;
			if (respForMsgId != null) {
				// 先处理等待锁逻辑
				WaitMsgLock lock = waitMsgQueueMap.get(respForMsgId);
				
				if (lock != null) {
					waitMsgQueueMap.remove(respForMsgId);
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
		LOG.debug("input {} Closed", session.getId());
		session.closeNow();
	}

	/**
	 * 增加消息等待
	 * 
	 * @author zhujun
	 * @date 2016年7月5日
	 *
	 * @param msgId
	 * @param lock
	 */
	public void addWaitMsgLock(String msgId, WaitMsgLock lock) {
		waitMsgQueueMap.put(msgId, lock);
	}

	public void removeWaitMsgLock(String msgId) {
		waitMsgQueueMap.remove(msgId);
	}

}
