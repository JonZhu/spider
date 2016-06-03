package com.zhujun.spider.master.domain;

import java.util.List;

/**
 * Action父级
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public interface DslParentAction extends DslAction {

	/**
	 * 子级
	 */
	List<DslAction> getChildren();
	
}
