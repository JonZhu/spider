package com.zhujun.spider.master.mina;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhujun.spider.master.data.db.IFetchUrlService;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.di.DIContext;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.schedule.IScheduleService;
import com.zhujun.spider.net.SpiderNetMessage;
import com.zhujun.spider.net.msgbody.PushUrlBody;
import com.zhujun.spider.net.msgbody.PushUrlBodyItem;

public class ServerHandler implements IoHandler {

	private final static Logger LOG = LoggerFactory.getLogger(ServerHandler.class);
	
	private IScheduleService scheduleService = DIContext.getInstance(IScheduleService.class);
	private IFetchUrlService fetchUrlService = DIContext.getInstance(IFetchUrlService.class);
	
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
				receiveFetchData(netMsg);
			}
			
		}
	}

	
	/**
	 * 接收到worker上传的抓取数据
	 * @param netMsg
	 */
	private void receiveFetchData(SpiderNetMessage netMsg) {
		String url = netMsg.getHeader("Fetch-url");
		boolean success = "Success".equals(netMsg.getHeader("Fetch-Result"));
		byte[] data = netMsg.getBody();
		String taskId = netMsg.getHeader("Task_id");
		String actionId = netMsg.getHeader("Action_id");
		String urlId = netMsg.getHeader("Url_id");
		
		
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
		SpiderNetMessage netMsg = new SpiderNetMessage();
		netMsg.setHeader("Action", "Push-url");
		netMsg.setHeader("Content-type", "json");
		String status = "200";
		
		Pair<String, Spider> task = scheduleService.randomScheduleTask();
		if (task != null) {
			netMsg.setHeader("Task-id", task.getLeft());
			try {
				List<FetchUrlPo> urlList = fetchUrlService.getGiveOutUrls(task.getRight().getDataDir());
				PushUrlBody body = new PushUrlBody();
				for (FetchUrlPo urlPo : urlList) {
					PushUrlBodyItem item = new PushUrlBodyItem();
					item.url = urlPo.getUrl();
					item.id = urlPo.getId();
					item.actionId = urlPo.getActionId();
					body.add(item);
				}
				
				netMsg.setBody(new ObjectMapper().writeValueAsBytes(body));
			} catch (Exception e) {
				status = "500";
				LOG.error("获取任务的下发url出错", e);
			}
			
		} else {
			// 无运行中的任务
			status = "404";
		}
		
		netMsg.setHeader("Status", status);
		
		session.write(netMsg);
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
