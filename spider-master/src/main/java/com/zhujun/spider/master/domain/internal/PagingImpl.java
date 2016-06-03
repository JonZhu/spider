package com.zhujun.spider.master.domain.internal;

import com.zhujun.spider.master.domain.Paging;

public class PagingImpl extends DslActionImpl implements Paging {

	private String select;
	
	private String urlAttr;
	
	@Override
	public String getSelect() {
		return select;
	}

	@Override
	public String getUrlAttr() {
		return urlAttr;
	}

	public void setSelect(String select) {
		this.select = select;
	}

	public void setUrlAttr(String urlAttr) {
		this.urlAttr = urlAttr;
	}
	

}
