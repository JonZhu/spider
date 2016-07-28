package com.zhujun.spider.worker.mina;

import java.net.SocketAddress;
import java.util.UUID;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.net.NetMessageCodecFactory;
import com.zhujun.spider.net.SpiderNetMessage;

/**
 * Mina实现的,与master通讯client
 * 
 * @author zhujun
 * @date 2016年6月21日
 *
 */
public class MinaClient {

	private final static Logger LOG = LoggerFactory.getLogger(MinaClient.class);
	
	private SocketAddress remoteAddress;
	private IoConnector connector;
	private IoSession session;
	private ClientHandler clientHandler;
	
	public MinaClient(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}


	public void start() {
		
		connector = new NioSocketConnector();
		
		connector.getFilterChain().addLast("logger", new LoggingFilter());
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new NetMessageCodecFactory()));
		
		clientHandler = new ClientHandler(); 
		connector.setHandler(clientHandler);
		
		connector.setConnectTimeoutMillis(5000);
		connectMaster();
		
	}


	private void connectMaster() {
		ConnectFuture connectFuture = null;
		
		while (true) {
			try {
				connectFuture = connector.connect(remoteAddress);
				connectFuture.awaitUninterruptibly();
				session = connectFuture.getSession();
				if (session.isConnected()) {
					break;
				}
			} catch (Exception e) {
				LOG.error("connect master fail, try again after 5 secend, reason：{}", e.getMessage());
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stop() {
		session = null;
		
		if (connector != null) {
			connector.dispose();
		}
	}


	public void sendMsg(SpiderNetMessage netMsg) {
		synchronized (this) {
			if (session == null) {
				throw new RuntimeException("Client未连接");
			}
			
			if (!session.isConnected()) {
				// 重连
				connectMaster();
			}
		}
		
		session.write(netMsg);
		
		LOG.debug("send {} message to master", netMsg.getHeader("Action"));
	}
	
	
	/**
	 * 发送消息, 并等待响应消息
	 * 
	 * @author zhujun
	 * @date 2016年7月28日
	 *
	 * @param netMsg
	 * @param responseTimeoutMs 等待响应消息超时时间
	 * @return 响应消息, 超时返回null
	 */
	public SpiderNetMessage sendMsg(SpiderNetMessage netMsg, long responseTimeoutMs) {
		String msgId = netMsg.getHeader("Msg-id");
		if (msgId == null) {
			msgId = UUID.randomUUID().toString();
			netMsg.setHeader("Msg-id", msgId);
		}
		
		WaitMsgLock lock = new WaitMsgLock();
		clientHandler.addWaitMsgLock(msgId, lock);
		
		sendMsg(netMsg);
		
		synchronized (lock) {
			if (lock.msg == null) { // 响应未被填充, msg可能在wait之前被填充
				try {
					lock.wait(responseTimeoutMs);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		// 防止超时后lock不能清除
		clientHandler.removeWaitMsgLock(msgId);
		
		return lock.msg;
	}
	
}
