package com.zhujun.spider.worker.mina;

import com.zhujun.spider.net.mina.NetMessageCodecFactory;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Mina实现的,与master通讯client
 * <p>被动方式</p>
 * 
 * @author zhujun
 * @date 2018年5月27日
 *
 */
public class MinaPassiveClient extends AbstractClient {

	private final static Logger LOG = LoggerFactory.getLogger(MinaPassiveClient.class);

	private int listenPort;
	private IoAcceptor acceptor;
	private volatile IoSession session;
	private ClientHandler clientHandler;

	/**
	 * 连接上master时通知等待线程
	 */
	private final Object connectMasterNofifier = new Object();

	public MinaPassiveClient(int listenPort) {
		this.listenPort = listenPort;
	}


	public void init() {
		
		acceptor = new NioSocketAcceptor();
		
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new NetMessageCodecFactory()));
		
		clientHandler = new ClientHandler(); 
		acceptor.setHandler(clientHandler);
		listenAcceptorEvent();

		try {
			acceptor.bind(new InetSocketAddress(listenPort));
		} catch (IOException e) {
			throw new RuntimeException("监听端口"+ listenPort +"出错", e);
		}

	}

	private void listenAcceptorEvent() {
		acceptor.addListener(new IoServiceListener() {
			@Override
			public void serviceActivated(IoService ioService) throws Exception {
				LOG.debug("serviceActivated");
			}

			@Override
			public void serviceIdle(IoService ioService, IdleStatus idleStatus) throws Exception {
				LOG.debug("serviceIdle :{}", idleStatus.toString());
			}

			@Override
			public void serviceDeactivated(IoService ioService) throws Exception {
				LOG.debug("serviceDeactivated");
			}

			@Override
			public void sessionCreated(IoSession ioSession) throws Exception {
				LOG.debug("session {} Created", ioSession.getId());
				if (isConnected()) {
					// 已经有一个master连接上，不允许第二个连接
					ioSession.closeNow();
					return;
				}

				MinaPassiveClient.this.session = ioSession;
				synchronized (connectMasterNofifier) {
					connectMasterNofifier.notifyAll();
				}
			}

			@Override
			public void sessionClosed(IoSession ioSession) throws Exception {
				LOG.debug("session {} Closed", ioSession.getId());
				if (MinaPassiveClient.this.session == ioSession) {
					MinaPassiveClient.this.session = null;
				}
			}

			@Override
			public void sessionDestroyed(IoSession ioSession) throws Exception {
				LOG.debug("session {} Destroyed", ioSession.getId());
				if (MinaPassiveClient.this.session == ioSession) {
					MinaPassiveClient.this.session = null;
				}
			}
		});
	}


	synchronized public void connectMaster() {
		while (true) {
			if (isConnected()) {
				// 已经连接上、退出
				InetSocketAddress masterAddress = (InetSocketAddress)session.getRemoteAddress();
				LOG.info("master({}:{}) 连接上worker", masterAddress.getHostName(), masterAddress.getPort());
				return;
			}

			LOG.info("等待master连接");
			synchronized (connectMasterNofifier) {
				try {
					connectMasterNofifier.wait(5000); // 等待5秒或其它线程notify
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void stop() {
		session = null;
		
		if (acceptor != null) {
			acceptor.dispose();
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
