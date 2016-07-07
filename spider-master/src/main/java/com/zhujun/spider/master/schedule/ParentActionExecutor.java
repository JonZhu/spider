package com.zhujun.spider.master.schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final static Logger LOG = LoggerFactory.getLogger(ParentActionExecutor.class);
	
	/**
	 * 执行children action
	 */
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
	
			String prentProgressStr = (String)dataScope.get(ScheduleConst.PROGRESS_KEY);
			
			for (int i = 0; i < children.size(); i++) {
				String progress = prentProgressStr == null ? String.valueOf(i) : prentProgressStr + ":" + i;
				dataScope.put(ScheduleConst.PROGRESS_KEY, progress);
				if (inHistoryProgress(dataScope)) {
					// 如果历史执行过, 则跳过
					continue;
				}
				
				LOG.debug("progress: {}", progress);
				persistDataScope(dataScope);
				
				DslAction child = children.get(i);
				ActionExecutor childExecutor = getActionExecutor(child);
				if (childExecutor == null) {
					throw new RuntimeException("找不到Action["+ child.getId() +"]的执行器");
				}
				
				context.setAction(child);
				childExecutor.execute(context);
			}
			
		}

	}

	/**
	 * 判断当前progress是否在history中
	 * @author zhujun
	 * @date 2016年7月7日
	 *
	 * @param dataScope
	 * @return
	 */
	private boolean inHistoryProgress(Map<String, Serializable> dataScope) {
		String hisProgress = (String)dataScope.get(ScheduleConst.HISTORY_PROGRESS_KEY);
		if (hisProgress == null) {
			// history progress 不存在
			return false;
		}
		
		// history progress存在
		
		if (hisProgress.startsWith((String)dataScope.get(ScheduleConst.PROGRESS_KEY))) {
			// 当前progress已经达到history, 可从这里执行
			dataScope.remove(ScheduleConst.HISTORY_PROGRESS_KEY); // 删除history标识
			return false;
		}
		
		return true;
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
			File persistFile = new File((String)dataScope.get(ScheduleConst.TASK_DATA_DIR_KEY), (String)dataScope.get(ScheduleConst.DATA_SCOPE_PERSISENT_NAME_KEY));
			oos = new ObjectOutputStream(new FileOutputStream(persistFile));
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
