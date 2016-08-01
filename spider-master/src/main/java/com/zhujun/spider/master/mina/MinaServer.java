package com.zhujun.spider.master.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.zhujun.spider.master.server.IServer;
import com.zhujun.spider.net.NetMessageCodecFactory;

/**
 * Mina实现的Shedule网络服务
 * 
 * @author zhujun
 * @date 2016年6月17日
 *
 */
public class MinaServer implements IServer {

	private int port;
	private IoAcceptor acceptor;
	
	public MinaServer(int port) {
		this.port = port;
	}
	
	public void start() {
		
		acceptor = new NioSocketAcceptor();

		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new NetMessageCodecFactory()));

		acceptor.setHandler(new ServerHandler());
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		try {
			acceptor.bind(new InetSocketAddress(port));
		} catch (IOException e) {
			throw new RuntimeException("启动Mina server失败", e);
		}
		
	}
	
	public void stop() {
		if (acceptor != null) {
			acceptor.dispose();
		}
	}
	
	/**
	 * 获取客户端Sessions
	 * 
	 * @author zhujun
	 * @date 2016年7月29日
	 *
	 * @return
	 */
	public Map<Long,IoSession> getClientSessions() {
		return acceptor == null ? null : acceptor.getManagedSessions();
	}
	
	
}
