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

        List<Map<String, Object>> workerList = new ArrayList<>();

        // 增加主动连接的worker
        Map<InetSocketAddress, IoSession> initiativeConnectMap = initiativeConnector.getSessionMap();
        if (initiativeConnectMap != null && !initiativeConnectMap.isEmpty()) {
            for (Map.Entry<InetSocketAddress, IoSession> entry : initiativeConnectMap.entrySet()) {
                Map<String, Object> workerInfo = null;
                if (entry.getValue() != null) {
                    // worker已创建session
                    workerInfo = getWorkerInfo(entry.getValue());
                } else {
                    // worker未关联session
                    workerInfo = new HashMap<>();
                    workerInfo.put("isConnected", false);
                }
                // 使用key address数据, session remote addresss 可能为null
                workerInfo.put("host", entry.getKey().getHostName());
                workerInfo.put("port", entry.getKey().getPort());

                workerInfo.put("connectSource", "master"); // 连接来源

                workerList.add(workerInfo);
            }
        }

        // worker连接上来的
        if (clientSessionMap != null && !clientSessionMap.isEmpty()) {
            List<Long> sessionIdList = new ArrayList<>(clientSessionMap.keySet());
            Collections.sort(sessionIdList); // sort

            for (Long sessionId : sessionIdList) {
                IoSession session = clientSessionMap.get(sessionId);
                Map<String, Object> workerInfo = getWorkerInfo(session);
                workerInfo.put("connectSource", "worker"); // 连接来源
                workerList.add(workerInfo);
            }
        }

        result.setData(workerList);
        return result;
    }

    private Map<String,Object> getWorkerInfo(IoSession session) {
        session.updateThroughput(System.currentTimeMillis(), true);
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("id", session.getId());

        InetSocketAddress address = (InetSocketAddress)session.getRemoteAddress();
        if (address != null) {
            clientData.put("host", address.getHostName());
            clientData.put("port", address.getPort());
        }

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

        // 状态
        clientData.put("isConnected", session.isConnected());

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
        initiativeConnector.addWorker(host, port);
        return result;
    }

    /**
     * 删除主动连接的客户端
     * @param host
     * @param port
     * @return
     */
    @RequestMapping(value = "/removeWorker", method = RequestMethod.DELETE)
    public Result removeWorker(String host, int port) {
        initiativeConnector.removeWorker(host, port);
        return new Result();
    }

}
