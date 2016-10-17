package com.zhujun.spider.net.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class NetMessageCodecFactory implements ProtocolCodecFactory {

	private final static ProtocolEncoder ENCODER = new NetMessageEncoder();
	private final static ProtocolDecoder DECODER = new NetMessageDecoder();
	
	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return ENCODER;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return DECODER;
	}

}
