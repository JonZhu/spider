package com.zhujun.spider.master.domain.internal;

import java.util.List;

import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.DslParentAction;

public class DslParentActionImpl extends DslActionImpl implements DslParentAction {

	private List<DslAction> children;
	
	public void setChildren(List<DslAction> children) {
		this.children = children;
	}

	@Override
	public List<DslAction> getChildren() {
		return children;
	}

}
