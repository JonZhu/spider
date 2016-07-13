package com.zhujun.spider.worker.mina;

import java.net.SocketAddress;

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
				LOG.error("连接服务端失败, 等待5秒重试, 原因：{}", e.getMessage());
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
		if (session == null) {
			throw new RuntimeException("Client未连接");
		}
		
		session.write(netMsg);
	}
	
	/**
	 * 等待消息
	 * 
	 * <p>阻塞等待一次某类型的消息, 超时返回null</p>
	 * 
	 * @author zhujun
	 * @date 2016年7月5日
	 *
	 * @param msgAction 消息的Action类型
	 * @param timeoutMs 超时毫秒
	 * @return
	 */
	public SpiderNetMessage waitMsg(String msgAction, long timeoutMs) {
		
		WaitMsgLock lock = new WaitMsgLock();
		synchronized (lock) {
			clientHandler.addWaitMsgLock(msgAction, lock);
			try {
				lock.wait(timeoutMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return lock.msg;
	}
	
}
