package com.zhujun.spider.net.mina;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class NetMessageDecoder extends CumulativeProtocolDecoder implements ProtocolDecoder {

	private final static AttributeKey PHASE_KEY = new AttributeKey(NetMessageDecoder.class, "phase");
	private final static AttributeKey MSG_KEY = new AttributeKey(NetMessageDecoder.class, "msg");
	private final static AttributeKey CHARSET_DECODER_KEY = new AttributeKey(NetMessageDecoder.class, "CharsetDecoder");
	
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		Phase phase = getPhaseFromSession(session);
		
		while (true) {
			if (phase == Phase.FIND_START) {
				int startIndex = findStart(in);
				if (startIndex == -1) {
					return false;
				} else {
					// msg start
					in.position(startIndex + Consts.MSG_START_BYTES.length);
					
					// 新开始msg对象
					SpiderNetMessage msg = new SpiderNetMessage();
					setMsgIntoSession(session, msg);
					
					phase = Phase.HEADER; // 进入header阶段
					setPhaseIntoSession(session, phase);
				}
				
			} else if (phase == Phase.HEADER) {
				
				int headerEnd = findHeaderEnd(in);
				if (headerEnd == -1) {
					return false;
				} else if (headerEnd == in.position()) {
					// header段结束
					in.skip(Consts.CRNL_BYTES.length);
					phase = Phase.BODY;
					setPhaseIntoSession(session, phase);
				} else {
					// header当前行结束
					String headerLine = in.getString(headerEnd - in.position(), getCharsetDecoder(session));
					in.position(headerEnd + Consts.CRNL_BYTES.length);
					
					String[] headerArray = headerLine.split(":", 2);
					SpiderNetMessage msg = getMsgFromSession(session);
					if (msg.getHeaders() == null) {
						msg.setHeaders(new HashMap<String, String>());
					}
					msg.getHeaders().put(headerArray[0], headerArray.length > 1 ? headerArray[1] : null);
				}
				
			} else if (phase == Phase.BODY) {
				SpiderNetMessage msg = getMsgFromSession(session);
				String bodyLenStr = msg.getHeaders() == null ? null : msg.getHeaders().get(SpiderNetMessage.HEADER_BODY_LENGTH);
				if (bodyLenStr == null || !NumberUtils.isNumber(bodyLenStr)) {
					// 无body
					phase = Phase.COMPLETE;
					setPhaseIntoSession(session, phase);
				} else {
					int bodyLength = Integer.parseInt(bodyLenStr);
					if (in.remaining() < bodyLength) {
						return false;
					}
					
					byte[] body = new byte[bodyLength];
					in.get(body);
					msg.setBody(body);
					phase = Phase.COMPLETE;
					setPhaseIntoSession(session, phase);
				}
			} else if (phase == Phase.COMPLETE) {
				out.write(getMsgFromSession(session));
				setMsgIntoSession(session, null);
				
				phase = Phase.FIND_START;
				setPhaseIntoSession(session, phase);
				return true;
			}
		}
		
	}

	
	private SpiderNetMessage getMsgFromSession(IoSession session) {
		return (SpiderNetMessage)session.getAttribute(MSG_KEY);
	}

	private void setMsgIntoSession(IoSession session, SpiderNetMessage msg) {
		session.setAttribute(MSG_KEY, msg);
	}

	private CharsetDecoder getCharsetDecoder(IoSession session) {
		CharsetDecoder decoder = (CharsetDecoder)session.getAttribute(CHARSET_DECODER_KEY);
		if (decoder == null) {
			decoder = Charset.forName("UTF-8").newDecoder();
			session.setAttribute(CHARSET_DECODER_KEY, decoder);
		}
		
		return decoder;
	}


	private int findHeaderEnd(IoBuffer in) {
		return findBytesInBuffer(in, Consts.CRNL_BYTES);
	}
	
	/**
	 * 从buffer中搜索 byte[]数据
	 * 
	 * @author zhujun
	 * @date 2016年6月20日
	 *
	 * @param buffer
	 * @param bytes
	 * @return
	 */
	private int findBytesInBuffer(IoBuffer buffer, byte[] bytes) {
		if (buffer.remaining() < bytes.length) {
			return -1;
		}
		
		for (int i = buffer.position(); i <= buffer.limit() - bytes.length; i++) {
			int matchCount = 0;
			for (int j = 0; j < bytes.length; j++) {
				if (bytes[j] == buffer.get(i + j)) {
					matchCount++;
				} else {
					break;
				}
			}
			
			if (matchCount == bytes.length) {
				return i;
			}
		}
		
		
		return -1;
	}
	
	
	private int findStart(IoBuffer in) {
		int index = findBytesInBuffer(in, Consts.MSG_START_BYTES);
		if (index < 0) {
			// 未搜索到, 舍弃搜索过的数据
			if (in.remaining() > Consts.MSG_START_BYTES.length) {
				in.skip(in.remaining() - Consts.MSG_START_BYTES.length + 1);
			}
		}

		return index;
	}
	

	private void setPhaseIntoSession(IoSession session, Phase phase) {
		session.setAttribute(PHASE_KEY, phase);
	}

	private Phase getPhaseFromSession(IoSession session) {
		Object attr = session.getAttribute(PHASE_KEY);
		if (attr == null) {
			attr = Phase.FIND_START;
			session.setAttribute(PHASE_KEY, attr);
		}
		
		return (Phase)attr;
	}


	/**
	 * 解析阶段
	 * 
	 * @author zhujun
	 * @date 2016年6月20日
	 *
	 */
	private enum Phase {
		FIND_START, HEADER, BODY, COMPLETE
	}

}
