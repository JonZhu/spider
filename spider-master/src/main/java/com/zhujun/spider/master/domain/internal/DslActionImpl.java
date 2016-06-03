package com.zhujun.spider.master.domain.internal;

import com.zhujun.spider.master.domain.DslAction;

public class DslActionImpl implements DslAction {

	private String name;
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
