package com.zhujun.spider.worker.mina;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.net.SpiderNetMessage;

public class ClientHandler implements IoHandler {

	private final static Logger LOG = LoggerFactory.getLogger(ClientHandler.class);
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		LOG.debug("sessionOpened");
		
		SpiderNetMessage msg = new SpiderNetMessage();
		Map<String, String> header = new HashMap<>();
		header.put("aaa", "111");
		header.put("bbb", "2222");
		msg.setHeaders(header);
		
		session.write(msg);
		
		LOG.debug("已发送测试msg");
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

}
