package com.zhujun.spider.master.data.db;

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

	void deleteSpiderTask(String taskId) throws Exception;

}
