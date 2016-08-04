package com.zhujun.spider.master.data.db;

import java.util.List;

import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.domain.Spider;

/**
 * Spider任务数据服务
 * 
 * @author zhujun
 * @date 2016年6月24日
 *
 */
public interface ISpiderTaskService {

	/**
	 * 创建任务
	 * 
	 * @author zhujun
	 * @date 2016年6月24日
	 *
	 * @param spider
	 * @throws Exception 
	 */
	void createSpiderTask(Spider spider) throws Exception;

	Page<SpiderTaskPo> findSpiderTaskList(int pageNo, int pageSize) throws Exception;
	
	/**
	 * 查询所有能调度的任务
	 * 
	 * @author zhujun
	 *
	 * @return
	 * @throws Exception
	 */
	List<SpiderTaskPo> findAllScheduleSpiderTask() throws Exception;

	void deleteSpiderTask(String taskId) throws Exception;

	/**
	 * 暂停任务
	 * 
	 * @author zhujun
	 * @date 2016年8月4日
	 *
	 * @param taskId
	 */
	void pauseTask(String taskId) throws Exception;

	/**
	 * 从暂停状态恢复任务
	 * 
	 * @author zhujun
	 * @date 2016年8月4日
	 *
	 * @param taskId
	 */
	void resumeTask(String taskId) throws Exception;

}
