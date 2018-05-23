package com.zhujun.spider.worker.mina;

import com.zhujun.spider.net.mina.SpiderNetMessage;
import com.zhujun.spider.worker.MasterClient;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 客户端抽象类
 *
 * @author zhujun
 * @desc AbstractClient
 * @time 2018/5/17 11:19
 */
public abstract class AbstractClient implements MasterClient {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractClient.class);


    abstract protected IoSession getSession();
    abstract protected ClientHandler getClientHandler();

    public void sendMsg(SpiderNetMessage netMsg) {
        if (!isConnected()) {
            // 检查并重连
            connectMaster();
        }
        getSession().write(netMsg);

        LOG.debug("send {} message to master", netMsg.getHeader("Action"));
    }


    /**
     * 发送消息, 并等待响应消息
     *
     * @author zhujun
     * @date 2016年7月28日
     *
     * @param netMsg
     * @param responseTimeoutMs 等待响应消息超时时间
     * @return 响应消息, 超时返回null
     */
    public SpiderNetMessage sendMsg(SpiderNetMessage netMsg, long responseTimeoutMs) {
        String msgId = netMsg.getHeader("Msg-id");
        if (msgId == null) {
            msgId = UUID.randomUUID().toString();
            netMsg.setHeader("Msg-id", msgId);
        }

        WaitMsgLock lock = new WaitMsgLock();
        ClientHandler clientHandler = getClientHandler();
        clientHandler.addWaitMsgLock(msgId, lock);

        sendMsg(netMsg);

        synchronized (lock) {
            if (lock.msg == null) { // 响应未被填充, msg可能在wait之前被填充
                try {
                    lock.wait(responseTimeoutMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // 防止超时后lock不能清除
        clientHandler.removeWaitMsgLock(msgId);

        return lock.msg;
    }

    @Override
    public boolean isConnected() {
        IoSession session = getSession();
        return session != null && session.isConnected();
    }

}
