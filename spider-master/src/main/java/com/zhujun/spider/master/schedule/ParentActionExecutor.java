package com.zhujun.spider.master.schedule;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.zhujun.spider.master.domain.DataTransition;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.DslParentAction;
import com.zhujun.spider.master.domain.Paging;
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
	public void execute(IScheduleContext context) throws Exception {
		DslAction action = context.getAction();
		
		if (action instanceof DslParentAction) {
			Map<String, Serializable> dataScope = context.getDataScope();
			DslParentAction parentAction = (DslParentAction)action;
			List<DslAction> children = parentAction.getChildren();
			if (children == null || children.isEmpty()) {
				return;
			}
	
			for (DslAction child : children) {
				ActionExecutor childExecutor = getActionExecutor(child);
				if (childExecutor == null) {
					throw new RuntimeException("找不到Action["+ child.getId() +"]的执行器");
				}
				
				context.setAction(child);
				childExecutor.execute(context);
				
				persistDataScope(dataScope);
			}
			
		}

	}

	/**
	 * 持久化数据
	 * @author zhujun
	 * @date 2016年7月7日
	 *
	 * @param dataScope
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private void persistDataScope(Map<String, Serializable> dataScope) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream("e:/tmp/datascope.bin"));
			oos.writeObject(dataScope);
			oos.flush();
		} finally {
			IOUtils.closeQuietly(oos);
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
