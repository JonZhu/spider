package com.zhujun.spider.master.controller;

import com.zhujun.spider.master.mina.InitiativeConnector;
import com.zhujun.spider.master.mina.MinaServer;
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

    @Autowired
    private InitiativeConnector initiativeConnector;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public Result queryWorker() {
        Map<Long, IoSession> clientSessionMap = minaServer.getClientSessions();

        Result result = new Result();
        Map<String, Object> data = new HashMap<>();

        // worker
        List<Map<String, Object>> workerList = new ArrayList<>();
        if (clientSessionMap != null && !clientSessionMap.isEmpty()) {

            List<Long> sessionIdList = new ArrayList<>(clientSessionMap.keySet());
            Collections.sort(sessionIdList); // sort

            for (Long sessionId : sessionIdList) {
                IoSession session = clientSessionMap.get(sessionId);
                workerList.add(getWorkerInfo(session));
            }
        }

        // 增加主动连接的worker
        for (IoSession session : initiativeConnector.getSessionMap().values()) {
            workerList.add(getWorkerInfo(session));
        }

        result.setData(workerList);
        return result;
    }

    private Map<String,Object> getWorkerInfo(IoSession session) {
        session.updateThroughput(System.currentTimeMillis(), true);
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("id", session.getId());

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

        return clientData;
    }


    /**
     * 连接worker
     * @param host
     * @param port
     * @return
     */
    @RequestMapping(value = "/connectWorker", method = RequestMethod.POST)
    public Result connectWorker(String host, int port) {
        Result result = new Result();
        IoSession session = initiativeConnector.connectWorker(host, port);
        if (session != null) {
            result.setData(getWorkerInfo(session));
        }

        return result;
    }

}
