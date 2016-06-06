package com.zhujun.spider.master.domain.internal;

import com.zhujun.spider.master.domain.DataWrite;

public class DataWriteImpl implements DataWrite {

	private String type;
	
	private String filename;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
