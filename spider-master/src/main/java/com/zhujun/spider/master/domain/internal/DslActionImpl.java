package com.zhujun.spider.master.domain.internal;

import com.zhujun.spider.master.domain.DslAction;

public class DslActionImpl implements DslAction {

	private String id;
	
	public void setId(String name) {
		this.id = name;
	}

	@Override
	public String getId() {
		return id;
	}

}
