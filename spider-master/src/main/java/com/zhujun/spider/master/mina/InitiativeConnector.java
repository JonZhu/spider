package com.zhujun.spider.master.mina;

import com.zhujun.spider.master.util.ThreadUtils;
import com.zhujun.spider.net.mina.NetMessageCodecFactory;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主动连接器
 *
 * <p>用于主动连接worker</p>
 *
 * @author zhujun
 * @desc InitiativeConnector
 * @time 2018/5/17 15:00
 */
@Component
public class InitiativeConnector {
    private static final Logger log = LoggerFactory.getLogger(InitiativeConnector.class);

    /**
     * InetSocketAddress -> session
     * InetSocketAddress -> string
     */
    private final Map<InetSocketAddress, Object> addressIoSessionMap = new ConcurrentHashMap<>();

    /**
     * 未连接状态
     */
    private final static String UN_CONNECTED = "unConnected";

    private final Object autoReConnectNotifier = new Object();

    private NioSocketConnector connector;

    @Autowired
    private ServerHandler serverHandler;

    @Value("${spider.mina.log-filter:true}")
    private boolean enableLogFilter;

    @PostConstruct
    private void init() {
        connector = new NioSocketConnector();
        if (enableLogFilter) {
            connector.getFilterChain().addLast("logger", new LoggingFilter());
        }
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new NetMessageCodecFactory()));
        connector.setHandler(serverHandler);
        connector.setConnectTimeoutMillis(5000);
        connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        connector.addListener(new IoServiceListener() {
            @Override
            public void serviceActivated(IoService ioService) throws Exception {
                log.debug("serviceActivated");
            }

            @Override
            public void serviceIdle(IoService ioService, IdleStatus idleStatus) throws Exception {
                log.debug("serviceIdle: {}", idleStatus.toString());
            }

            @Override
            public void serviceDeactivated(IoService ioService) throws Exception {
                log.debug("serviceDeactivated");
            }

            @Override
            public void sessionCreated(IoSession ioSession) throws Exception {
                log.info("session {} Created", ioSession.getId());
                InetSocketAddress workerAdress = (InetSocketAddress)ioSession.getRemoteAddress();
                synchronized (addressIoSessionMap) {
                    if (addressIoSessionMap.containsKey(workerAdress)) {
                        addressIoSessionMap.put((InetSocketAddress)ioSession.getRemoteAddress(), ioSession);
                    } else {
                        log.debug("worker({}:{})已经被删除, 关闭session", workerAdress.getHostName(), workerAdress.getPort());
                        // 关闭session
                        ioSession.closeNow();
                    }
                }
            }

            @Override
            public void sessionClosed(IoSession ioSession) throws Exception {
                log.info("session {} Closed", ioSession.getId());
                onWorkerSessionOffline(ioSession);
            }

            @Override
            public void sessionDestroyed(IoSession ioSession) throws Exception {
                log.info("session {} Destroyed", ioSession.getId());
                onWorkerSessionOffline(ioSession);
            }
        });

        // 启动重连线程
        new AutoReConnectThread(connector, addressIoSessionMap, autoReConnectNotifier).start();
    }

    private void validateWorkerExist(InetSocketAddress workerAddress) {
        synchronized (addressIoSessionMap) {
            if (addressIoSessionMap.containsKey(workerAddress)) {
                throw new RuntimeException("worker已存在");
            }
        }
    }

    /**
     * 添加workor
     * @param host
     * @param port
     */
    public void addWorker(String host, int port) {
        InetSocketAddress workerAddress = new InetSocketAddress(host, port);
        validateWorkerExist(workerAddress);
        addressIoSessionMap.put(workerAddress, UN_CONNECTED); // 添加到map

        // 通知重连线程
        synchronized (autoReConnectNotifier) {
            autoReConnectNotifier.notifyAll();
        }
    }

    /**
     * 删除worker
     *
     * @param host
     * @param port
     */
    public void removeWorker(String host, int port) {
        addressIoSessionMap.remove(new InetSocketAddress(host, port));
    }

    /**
     * session掉线
     * @param ioSession
     */
    public void onWorkerSessionOffline(IoSession ioSession) {
        // 设置为null，连接线程会重连
        InetSocketAddress workerAddress = (InetSocketAddress)ioSession.getRemoteAddress();
        boolean needNotify = false; // 是否需要通知重连线程
        synchronized (addressIoSessionMap) {
            if (addressIoSessionMap.containsKey(workerAddress)) { // 判断key，以防worker其它地方删除后，在这里又添加
                addressIoSessionMap.put(workerAddress, UN_CONNECTED);
                needNotify = true;
            }
        }

        if (needNotify) {
            synchronized (autoReConnectNotifier) {
                autoReConnectNotifier.notifyAll(); // 通知重连线程
            }
        }
    }

    public Map<InetSocketAddress, IoSession> getSessionMap() {
        if (addressIoSessionMap.isEmpty()) {
            return null;
        }

        Map<InetSocketAddress, IoSession> sessionMap = new HashMap<>();
        for (Map.Entry<InetSocketAddress, Object> entry : addressIoSessionMap.entrySet()) {
            sessionMap.put(entry.getKey(), entry.getValue() instanceof IoSession ? (IoSession)entry.getValue() : null);
        }
        return sessionMap;
    }

    /**
     * 自动重连线程
     */
    private static class AutoReConnectThread extends Thread {
        private final NioSocketConnector connector;
        private final Map<InetSocketAddress, Object> addressIoSessionMap;
        private final Object notifier;

        public AutoReConnectThread(NioSocketConnector connector, Map<InetSocketAddress, Object> addressIoSessionMap, Object notifier) {
            super("AutoReConnectWorker");
            this.connector = connector;
            this.addressIoSessionMap = addressIoSessionMap;
            this.notifier = notifier;
        }

        @Override
        public void run() {
            List<InetSocketAddress> needReConnectList = null;
            while (true) {
                needReConnectList = null;
                if (!addressIoSessionMap.isEmpty()) {
                    needReConnectList = new ArrayList<>();
                    for(Map.Entry<InetSocketAddress, Object> entry : addressIoSessionMap.entrySet()) {
                        if (entry.getValue() == UN_CONNECTED) {
                            // 需要重连的address，加入列表
                            needReConnectList.add(entry.getKey());
                        } else {
                            IoSession session = (IoSession)entry.getValue();
                            if (!session.isConnected()) {
                                needReConnectList.add(entry.getKey());
                            }
                        }
                    }
                }

                if (needReConnectList == null || needReConnectList.isEmpty()) {
                    log.debug("没有需要重连的worker，wait");
                    synchronized (notifier) {
                        try {
                            notifier.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    continue;
                }

                for(InetSocketAddress address : needReConnectList) {
                    // 重连
                    log.debug("连接worker {}:{}", address.getHostName(), address.getPort());
                    connector.connect(address);
                }

                ThreadUtils.sleep(5000); // 等待5秒
            }
        }
    }

}
