package com.zhujun.spider.master.schedule;

import java.util.List;
import java.util.Map;

import com.zhujun.spider.master.domain.DataTransition;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.DslParentAction;
import com.zhujun.spider.master.domain.Paging;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.Url;
import com.zhujun.spider.master.domain.UrlSet;

/**
 * 主要执行children
 * 
 * @author zhujun
 * @date 2016年6月4日
 *
 */
public abstract class ParentActionExecutor implements ActionExecutor {

	@Override
	public void execute(Spider spider, DslAction action, Map<String, Object> dataScope) throws Exception {
		
		if (action instanceof DslParentAction) {
			DslParentAction parentAction = (DslParentAction)action;
			List<DslAction> children = parentAction.getChildren();
			if (children == null || children.isEmpty()) {
				return;
			}
	
			for (DslAction child : children) {
				ActionExecutor childExecutor = getActionExecutor(child);
				if (childExecutor == null) {
					throw new RuntimeException("找不到Action["+ child.getName() +"]的执行器");
				}
				childExecutor.execute(spider, child, dataScope);
			}
			
		}

	}

	private ActionExecutor getActionExecutor(DslAction action) {
		if (action == null) {
			return null;
		}
		
		if (action instanceof Url) {
			return new UrlExecutor();
		}
		
		if (action instanceof UrlSet) {
			return new UrlSetExecutor();
		}
		
		if (action instanceof DataTransition) {
			return new DataTransitionExecutor();
		}
		
		if (action instanceof Paging) {
			return new PagingExecutor();
		}
		
		
		return null;
	}

}
