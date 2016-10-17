package com.zhujun.spider.net.mina;

import java.util.HashMap;
import java.util.Map;

/**
 * Spider网络传输数据消息
 * 
 * @author zhujun
 * @date 2016年6月17日
 *
 */
public class SpiderNetMessage {
	
	public final static String HEADER_BODY_LENGTH = "Content-Length";

	private Map<String, String> headers;
	
	private byte[] body;

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
	
	
	public void setHeader(String name, String value) {
		if (headers == null) {
			headers = new HashMap<>();
		}
		headers.put(name, value);
	}
	
	public String getHeader(String name) {
		return headers == null ? null : headers.get(name);
	}
	
}
