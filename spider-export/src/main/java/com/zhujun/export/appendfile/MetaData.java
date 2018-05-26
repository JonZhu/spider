package com.zhujun.export.appendfile;

import java.util.Date;

/**
 * AppendFile中的文件元数据
 * 
 * @author zhujun
 * @date 2016年11月4日
 *
 */
public class MetaData {

	private String url;
	
	private String contentType;
	
	private long size;
	
	private Date fetchTime;

	/**
	 * 在append文件中的偏移位置, 通过该文件可以直接读到文件MetaData
	 */
	private long offset;

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Date getFetchTime() {
		return fetchTime;
	}

	public void setFetchTime(Date fetchTime) {
		this.fetchTime = fetchTime;
	}
	
}
