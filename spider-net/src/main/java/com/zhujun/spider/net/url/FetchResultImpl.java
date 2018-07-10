package com.zhujun.spider.net.url;

import java.util.Map;

public class FetchResultImpl implements IFetchResult {

	private String contentType;
	
	private byte[] data;

	private int httpStatusCode;

	private Map<String, String> headers;

	public String getContentType() {
		return headers == null ? null : headers.get("Content-Type");
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	@Override
	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
}
