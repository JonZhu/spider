package com.zhujun.spider.worker.mina;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.zhujun.spider.net.NetMessageCodecFactory;

/**
 * Mina实现的,与master通讯client
 * 
 * @author zhujun
 * @date 2016年6月21日
 *
 */
public class MinaClient {

	
	public void start() {
		
		IoConnector connector = new NioSocketConnector();
		
		connector.getFilterChain().addLast("logger", new LoggingFilter());
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new NetMessageCodecFactory()));
		
		
		
	}
	
	
}
