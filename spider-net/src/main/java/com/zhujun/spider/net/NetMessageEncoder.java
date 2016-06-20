package com.zhujun.spider.net;

import java.io.ByteArrayOutputStream;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class NetMessageEncoder extends ProtocolEncoderAdapter implements ProtocolEncoder {

	
	
	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {

		SpiderNetMessage netMessage = (SpiderNetMessage)message;
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(Consts.MSG_START_BYTES); // 开始标识
		
		if (netMessage.getHeaders() != null && !netMessage.getHeaders().isEmpty()) {
			Set<Entry<String, String>> headerEntrySet = netMessage.getHeaders().entrySet();
			for (Entry<String, String> headerEntry : headerEntrySet) {
				outputStream.write((headerEntry.getKey() + ":" + headerEntry.getValue()).getBytes());
			}
		}
		
		if (netMessage.getBody() != null) {
			outputStream.write((SpiderNetMessage.HEADER_BODY_LENGTH + ":" + netMessage.getBody().length).getBytes());
		}
		outputStream.write(Consts.CRNL.getBytes()); // header结束
		
		if (netMessage.getBody() != null) {
			// 写入body数据
			outputStream.write(netMessage.getBody());
		}
		
		out.write(IoBuffer.wrap(outputStream.toByteArray()));
		
	}

}
