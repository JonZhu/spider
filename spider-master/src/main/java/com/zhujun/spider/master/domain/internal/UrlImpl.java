package com.zhujun.spider.master.domain.internal;

import com.zhujun.spider.master.domain.Url;

public class UrlImpl extends DslParentActionImpl implements Url {

	private String href;
	
	public void setHref(String href) {
		this.href = href;
	}

	@Override
	public String getHref() {
		return href;
	}

}
