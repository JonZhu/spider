package com.zhujun.spider.master.controller;

import com.zhujun.spider.master.mina.MinaServer;
import org.apache.mina.core.service.IoServiceStatistics;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * @author zhujun
 * @desc WorkerController
 * @time 2018/5/16 15:23
 */
@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    @Autowired
    private MinaServer minaServer;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public Result queryWorker() {
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
        return result;
    }

}
