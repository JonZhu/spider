package com.zhujun.spider.master.schedule;

import java.io.Serializable;
import java.util.Map;

/**
 * 执行进度工具
 * 
 * @author zhujun
 * @date 2016年7月11日
 *
 */
public class ProgressUtils {

	/**
	 * 判断当前progress是否在history中
	 * @author zhujun
	 * @date 2016年7月7日
	 *
	 * @param dataScope
	 * @return
	 */
	public static boolean inHistoryProgress(Map<String, Serializable> dataScope) {
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
	
}
