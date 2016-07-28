package com.zhujun.spider.master.schedule.progress;

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

import com.zhujun.spider.master.schedule.IScheduleContext;
import com.zhujun.spider.master.schedule.ScheduleConst;

/**
 * 执行进度工具
 * 
 * @author zhujun
 * @date 2016年7月11日
 *
 */
public class ProgressUtils {

	private final static Logger LOG = LoggerFactory.getLogger(ProgressUtils.class);
	
	/**
	 * 判断当前progress是否在history中
	 * @author zhujun
	 * @date 2016年7月7日
	 *
	 * @param dataScope
	 * @return
	 */
	private static boolean inHistoryProgress(Map<String, Serializable> dataScope) {
		String hisProgress = (String)dataScope.get(ScheduleConst.HISTORY_PROGRESS_KEY);
		if (hisProgress == null) {
			// history progress 不存在
			return false;
		}
		
		// history progress存在
		
		String progress = (String)dataScope.get(ScheduleConst.PROGRESS_KEY);
		if (hisProgress.startsWith(progress)) {
			// 当前progress已经达到history 执行路径, 可从这里执行
			if (hisProgress.equals(progress)) { // 到达history上次退出点
				dataScope.remove(ScheduleConst.HISTORY_PROGRESS_KEY); // 删除history标识
			}
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 执行步骤
	 * 
	 * <p>保存executor内部progress记录</p>
	 * 
	 * @author zhujun
	 * @date 2016年7月11日
	 *
	 * @param stepList
	 * @param progressSeparator progress字符串, 父子级分隔符, action用':', action内部用'_'
	 * @throws Exception 
	 */
	public static void executeSteps(IScheduleContext context, List<IStep> stepList, char progressSeparator) throws Exception {
		Map<String, Serializable> dataScope = context.getDataScope();
		String progress = (String)dataScope.get(ScheduleConst.PROGRESS_KEY);
		
		for (int i = 0; i < stepList.size(); i++) {
			String stepProgress = progress == null ? String.valueOf(i) : progress + progressSeparator + i;
			dataScope.put(ScheduleConst.PROGRESS_KEY, stepProgress);
			if (ProgressUtils.inHistoryProgress(dataScope)) {
				// 如果历史执行过, 则跳过
				continue;
			}
			
			persistDataScope(dataScope);
			
			LOG.debug("start execute progress: {}", stepProgress);
			
			stepList.get(i).execute(context);
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
	private static void persistDataScope(Map<String, Serializable> dataScope) throws FileNotFoundException, IOException {
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
	
	
}
