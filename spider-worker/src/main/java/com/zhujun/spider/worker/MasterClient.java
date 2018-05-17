package com.zhujun.spider.worker;

import com.zhujun.spider.net.mina.SpiderNetMessage;

/**
 * 主结点客户端
 *
 * @author zhujun
 * @desc MasterClient
 * @time 2018/5/17 11:16
 */
public interface MasterClient {
    /**
     * 初始化
     */
    void init();

    /**
     * 连接master，直到成功
     */
    void connectMaster();

    /**
     * 是否连接上master
     * @return
     */
    boolean isConnected();

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
    SpiderNetMessage sendMsg(SpiderNetMessage netMsg, long responseTimeoutMs);

    void sendMsg(SpiderNetMessage netMsg);

}
