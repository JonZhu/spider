package com.zhujun.spider.master.mina;

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
import java.util.Collections;
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

    private final Map<Long, IoSession> sessionMap = new ConcurrentHashMap<>();
    private final Map<InetSocketAddress, IoSession> addressIoSessionMap = new ConcurrentHashMap<>();

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
                sessionMap.put(ioSession.getId(), ioSession);
            }

            @Override
            public void sessionClosed(IoSession ioSession) throws Exception {
                log.info("session {} Closed", ioSession.getId());
                sessionMap.remove(ioSession.getId());
            }

            @Override
            public void sessionDestroyed(IoSession ioSession) throws Exception {
                log.info("session {} Destroyed", ioSession.getId());
                sessionMap.remove(ioSession.getId());
            }
        });
    }

    public IoSession connectWorker(String host, int port) {
        InetSocketAddress workerAddress = new InetSocketAddress(host, port);
        validateWorkerExist(workerAddress);
        IoSession session = null;
        try {
            session = connector.connect(workerAddress).await().getSession();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return session;
    }

    private void validateWorkerExist(InetSocketAddress workerAddress) {
        if (addressIoSessionMap.containsKey(workerAddress)) {
            throw new RuntimeException("worker已存在");
        }
    }

    /**
     * 添加自动重连work
     * @param host
     * @param port
     */
    public void autoReConnectWorker(String host, int port) {
        InetSocketAddress workerAddress = new InetSocketAddress(host, port);
        validateWorkerExist(workerAddress);
        addressIoSessionMap.put(workerAddress, null); // 添加到map
        IoSession session = null;
        try {
            session = connector.connect(new InetSocketAddress(host, port)).await().getSession();
            // 设置自动重连
            session.setAttribute("reConnect", true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Map<Long, IoSession> getSessionMap() {
        return Collections.unmodifiableMap(this.sessionMap);
    }


    /**
     * 自动重连线程
     */
    private static class AutoReConnectThread extends Thread {
        private final NioSocketConnector connector;
        private final Map<InetSocketAddress, IoSession> addressIoSessionMap;
        private final Object notifier;

        public AutoReConnectThread(NioSocketConnector connector, Map<InetSocketAddress, IoSession> addressIoSessionMap, Object notifier) {
            super("AutoReConnectWorker");
            this.connector = connector;
            this.addressIoSessionMap = addressIoSessionMap;
            this.notifier = notifier;
        }

        @Override
        public void run() {
            while (true) {
                if (addressIoSessionMap.isEmpty()) {
                    synchronized (notifier) {
                        try {
                            notifier.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                for(Map.Entry<InetSocketAddress, IoSession> entry : addressIoSessionMap.entrySet()) {
                    if (entry.getValue() == null) {
                        // address未分配到session，需要重连
                        connector.connect(entry.getKey());
                    }
                }

                // todo 重连
            }

        }
    }

}
