package com.zhujun.spider.master.ui.servlet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.mina.core.service.IoServiceStatistics;
import org.apache.mina.core.session.IoSession;

import com.zhujun.spider.master.di.DIContext;
import com.zhujun.spider.master.mina.MinaServer;
import com.zhujun.spider.master.ui.JsonUtils;
import com.zhujun.spider.master.ui.Result;

/**
 * worker列表
 * 
 * @author zhujun
 * @date 2016年7月29日
 *
 */
public class WorkerListServlet  extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8316205492333450882L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MinaServer minaServer = DIContext.getInstance(MinaServer.class);
		Map<Long, IoSession> clientSessionMap = minaServer.getClientSessions();
		
		Result result = new Result();
		Map<String, Object> data = new HashMap<>();
		
		// worker
		if (clientSessionMap != null && !clientSessionMap.isEmpty()) {
			List<Map<String, Object>> workerList = new ArrayList<>();
			
			List<Long> sessionIdList = new ArrayList<>(clientSessionMap.keySet());
			Collections.sort(sessionIdList); // sort
			
			for (Long sessionId : sessionIdList) {
				IoSession session = clientSessionMap.get(sessionId);
				session.updateThroughput(System.currentTimeMillis(), true);
				Map<String, Object> clientData = new HashMap<>();
				clientData.put("id", sessionId);
				
				InetSocketAddress address = (InetSocketAddress)session.getRemoteAddress();
				clientData.put("host", address.getHostName());
				clientData.put("port", address.getPort());
				
				clientData.put("connectTime", session.getCreationTime());
				
				// 下行
				clientData.put("downBytes", session.getWrittenBytes());
				clientData.put("downBytesPS", session.getWrittenBytesThroughput());
				clientData.put("downMsg", session.getWrittenMessages());
				clientData.put("downMsgPS", session.getWrittenMessagesThroughput());
				
				// 上行
				clientData.put("upBytes", session.getReadBytes());
				clientData.put("upBytesPS", session.getReadBytesThroughput());
				clientData.put("upMsg", session.getReadMessages());
				clientData.put("upMsgPS", session.getReadMessagesThroughput());
				
				workerList.add(clientData);
			}
			
			data.put("workerList", workerList);
		}
		
		// 累计数据
		IoServiceStatistics acceptorStatis = minaServer.getAcceptorStatistics();
		if (acceptorStatis != null && acceptorStatis.getReadBytes() > 0) {
			acceptorStatis.updateThroughput(System.currentTimeMillis());
			Map<String, Object> accumTotal = new HashMap<>();
			// 下行
			accumTotal.put("downBytes", acceptorStatis.getWrittenBytes());
			accumTotal.put("downBytesPS", acceptorStatis.getWrittenBytesThroughput());
			accumTotal.put("downMsg", acceptorStatis.getWrittenMessages());
			accumTotal.put("downMsgPS", acceptorStatis.getWrittenMessagesThroughput());
			
			// 上行
			accumTotal.put("upBytes", acceptorStatis.getReadBytes());
			accumTotal.put("upBytesPS", acceptorStatis.getReadBytesThroughput());
			accumTotal.put("upMsg", acceptorStatis.getReadMessages());
			accumTotal.put("upMsgPS", acceptorStatis.getReadMessagesThroughput());
			
			data.put("accumTotal", accumTotal);
		}
		
		result.setData(data);
		JsonUtils.writeValue(resp.getOutputStream(), result);
	}
}
