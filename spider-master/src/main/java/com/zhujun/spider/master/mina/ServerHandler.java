package com.zhujun.spider.master.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhujun.spider.master.data.db.IFetchUrlService;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.schedule.IScheduleService;
import com.zhujun.spider.master.schedule.PushDataQueue;
import com.zhujun.spider.master.schedule.PushDataQueue.Item;
import com.zhujun.spider.net.mina.SpiderNetMessage;
import com.zhujun.spider.net.mina.msgbody.PushUrlBody;
import com.zhujun.spider.net.mina.msgbody.PushUrlBodyItem;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ServerHandler implements IoHandler {

	private final static Logger LOG = LoggerFactory.getLogger(ServerHandler.class);

	@Autowired
	private IScheduleService scheduleService;

	@Autowired
	private IFetchUrlService fetchUrlService;
	
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
		LOG.debug("session {} Idle: {}", session.getId(), status.toString());

	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		LOG.debug("exceptionCaught, session: {}", session.getId(), cause);

	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (message instanceof SpiderNetMessage) {
			SpiderNetMessage netMsg = (SpiderNetMessage)message;
			String action = netMsg.getHeader("Action");
			LOG.debug("messageReceived SpiderNetMessage {}", action);
			
			if ("Pull-url".equals(action)) {
				pushUrl2client(session, netMsg);
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
		
		Item item = new Item();
		item.data = data;
		item.success = success;
		item.url = url;
		item.fetchTime = new Date(Long.parseLong(netMsg.getHeader("Fetch-time")));
		item.taskId = taskId;
		item.actionId = actionId;
		item.urlId = urlId;
		item.contentType = netMsg.getHeader("Content-Type");
		item.httpStatusCode = Integer.parseInt(netMsg.getHeader("StatusCode"));
		
		// 写入数据上传队列, 等待其它线程处理
		PushDataQueue.addPushData(taskId, actionId, item);
	}

	/**
	 * 推送url给客户端
	 * 
	 * @author zhujun
	 * @date 2016年6月22日
	 *
	 * @param session
	 */
	private void pushUrl2client(IoSession session, SpiderNetMessage requstMsg) {
		SpiderNetMessage netMsg = new SpiderNetMessage();
		netMsg.setHeader("Action", "Push-url");
		netMsg.setHeader("Content-type", "json");
		String status = "200";
		
		Pair<String, SpiderTaskPo> task = scheduleService.randomRunningScheduleTask();
		if (task != null) {
			String taskId = task.getLeft();
			netMsg.setHeader("Task-id", taskId);
			final int speedLimitCount = 10000; // PushDataQueue中数据量多于该值限速
			try {
				if (PushDataQueue.getDataCount(taskId) < speedLimitCount) {
					List<FetchUrlPo> urlList = fetchUrlService.getGiveOutUrls(task.getRight());
					PushUrlBody body = new PushUrlBody();
					for (FetchUrlPo urlPo : urlList) {
						PushUrlBodyItem item = new PushUrlBodyItem();
						item.url = urlPo.getUrl();
						item.id = urlPo.getId();
						item.actionId = urlPo.getActionId();
						body.add(item);
					}

					netMsg.setBody(new ObjectMapper().writeValueAsBytes(body));
				} else {
					LOG.warn("任务{} PushDataQueue中未处理数据达到 {}, 暂不pushUrl", taskId, speedLimitCount);
				}
			} catch (Exception e) {
				status = "500";
				LOG.error("获取任务的下发url出错", e);
			}
			
		} else {
			// 无运行中的任务
			status = "404";
		}
		
		netMsg.setHeader("Status", status);
		
		// 处理响应消息id
		String reqMsgId = requstMsg.getHeader("Msg-id");
		if (reqMsgId != null) {
			netMsg.setHeader("Response-for", reqMsgId);
		}
		
		session.write(netMsg);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		LOG.debug("inputClosed session {} Closed", session.getId());
		session.closeNow();
	}

}
