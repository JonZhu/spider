package com.zhujun.spider.worker.mina;

import java.net.SocketAddress;
import java.util.UUID;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.net.mina.NetMessageCodecFactory;
import com.zhujun.spider.net.mina.SpiderNetMessage;

/**
 * Mina实现的,与master通讯client
 * <p>主动方式</p>
 * 
 * @author zhujun
 * @date 2016年6月21日
 *
 */
public class MinaInitiativeClient extends AbstractClient {

	private final static Logger LOG = LoggerFactory.getLogger(MinaInitiativeClient.class);
	
	private SocketAddress remoteAddress;
	private IoConnector connector;
	private IoSession session;
	private ClientHandler clientHandler;
	
	public MinaInitiativeClient(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}


	public void init() {
		
		connector = new NioSocketConnector();
		
		connector.getFilterChain().addLast("logger", new LoggingFilter());
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new NetMessageCodecFactory()));
		
		clientHandler = new ClientHandler(); 
		connector.setHandler(clientHandler);
		
		connector.setConnectTimeoutMillis(5000);
		connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
	}


	public void connectMaster() {
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
				LOG.error("connect master fail, try again after 5 secend, reason: {}", e.getMessage());
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


	@Override
	protected IoSession getSession() {
		return this.session;
	}

	@Override
	protected ClientHandler getClientHandler() {
		return this.clientHandler;
	}
}
