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
import java.util.Collections;
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

    public NioSocketConnector connector;

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

            }

            @Override
            public void serviceIdle(IoService ioService, IdleStatus idleStatus) throws Exception {

            }

            @Override
            public void serviceDeactivated(IoService ioService) throws Exception {

            }

            @Override
            public void sessionCreated(IoSession ioSession) throws Exception {
                log.info("add session {}", ioSession.getId());
                sessionMap.put(ioSession.getId(), ioSession);
            }

            @Override
            public void sessionClosed(IoSession ioSession) throws Exception {
                log.info("remove session {}", ioSession.getId());
                sessionMap.remove(ioSession.getId());
            }

            @Override
            public void sessionDestroyed(IoSession ioSession) throws Exception {
                log.info("remove session {}", ioSession.getId());
                sessionMap.remove(ioSession.getId());
            }
        });
    }

    public IoSession connectWorker(String host, int port) {
        IoSession session = null;
        try {
            session = connector.connect(new InetSocketAddress(host, port)).await().getSession();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return session;
    }

    public Map<Long, IoSession> getSessionMap() {
        return Collections.unmodifiableMap(this.sessionMap);
    }

}
