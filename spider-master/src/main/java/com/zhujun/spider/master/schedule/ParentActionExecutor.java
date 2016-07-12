package com.zhujun.spider.master.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.domain.DataTransition;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.DslParentAction;
import com.zhujun.spider.master.domain.Paging;
import com.zhujun.spider.master.domain.Url;
import com.zhujun.spider.master.domain.UrlSet;
import com.zhujun.spider.master.schedule.progress.IStep;
import com.zhujun.spider.master.schedule.progress.ProgressUtils;

/**
 * 主要执行children
 * 
 * @author zhujun
 * @date 2016年6月4日
 *
 */
public abstract class ParentActionExecutor implements ActionExecutor {

	private final static Logger LOG = LoggerFactory.getLogger(ParentActionExecutor.class);
	
	/**
	 * 执行children action
	 */
	@Override
	public void execute(IScheduleContext context) throws Exception {
		DslAction action = context.getAction();
		
		if (action instanceof DslParentAction) {
			DslParentAction parentAction = (DslParentAction)action;
			List<DslAction> children = parentAction.getChildren();
			if (children == null || children.isEmpty()) {
				return;
			}
	
			List<IStep> actionStepList = new ArrayList<>();
			
			for (int i = 0; i < children.size(); i++) {
				final DslAction child = children.get(i);
				
				// 将action执行封装到step中，支持progress保存
				IStep actionStep = new IStep() {
					@Override
					public void execute(IScheduleContext c) throws Exception {
						// TODO Auto-generated method stub
						
						ActionExecutor childExecutor = getActionExecutor(child);
						if (childExecutor == null) {
							throw new RuntimeException("找不到Action["+ child.getId() +"]的执行器");
						}
						
						c.setAction(child);
						childExecutor.execute(c);
					}
				};
				actionStepList.add(actionStep);
				
			}
			
			ProgressUtils.executeSteps(context, actionStepList, ':');
			
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
