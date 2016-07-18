package com.zhujun.spider.worker.fetch;

public class FetchResultImpl implements IFetchResult {

	private String contentType;
	
	private byte[] data;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
}
