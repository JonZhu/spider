package com.zhujun.spider.master.mina;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.zhujun.spider.net.SpiderNetMessage;

public class ServerHandler implements IoHandler {

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (message instanceof SpiderNetMessage) {
			System.out.println(ToStringBuilder.reflectionToString(message));
			SpiderNetMessage netMsg = (SpiderNetMessage)message;
			String action = netMsg.getHeader("Action");
			
			if ("Pull-url".equals(action)) {
				pushUrl2client(session);
			} else if ("Push-fetch-data".equals(action)) {
				
			}
			
		}
	}

	/**
	 * 推送url给客户端
	 * 
	 * @author zhujun
	 * @date 2016年6月22日
	 *
	 * @param session
	 */
	private void pushUrl2client(IoSession session) {
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
