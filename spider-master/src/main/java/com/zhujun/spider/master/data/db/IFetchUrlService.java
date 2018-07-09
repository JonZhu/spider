package com.zhujun.spider.master.data.db;

import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.exception.ExceptionIgnore;

import java.util.Date;
import java.util.List;

/**
 * 抓取url服务
 * 
 * @author zhujun
 * @date 2016年7月3日
 *
 */
public interface IFetchUrlService {

	/**
	 * 创建url
	 * 
	 * @author zhujun
	 * @date 2016年7月4日
	 *
	 * @param task
	 * @param fetchUrl
	 * @return
	 * @throws Exception
	 * @return insertCount
	 */
	int createFetchUrl(SpiderTaskPo task, FetchUrlPo fetchUrl) throws Exception;
	
	/**
	 * 批量创建url
	 * 
	 * @author zhujun
	 * @date 2016年7月5日
	 *
	 * @param task
	 * @param fetchUrlList
	 * @throws Exception
	 * @return insertCount
	 */
	int createFetchUrl(SpiderTaskPo task, List<FetchUrlPo> fetchUrlList) throws Exception;

	/**
	 * 批量创建url
	 *
	 * @param task
	 * @param fetchUrlList
	 * @param exceptionIgnore
	 * @throws Exception
	 * @return insertCount
	 */
	int createFetchUrl(SpiderTaskPo task, List<FetchUrlPo> fetchUrlList, ExceptionIgnore exceptionIgnore) throws Exception;
	
	/**
	 * 获取要分发的url
	 * 
	 * @author zhujun
	 * @date 2016年7月4日
	 *
	 * @param task
	 * @return
	 * @throws Exception 
	 */
	List<FetchUrlPo> getGiveOutUrls(SpiderTaskPo task) throws Exception;

	/**
	 * 判断action中是否还有未抓取的url
	 * 
	 * @author zhujun
	 * @date 2016年7月8日
	 *
	 * @param task
	 * @param actionId
	 * @return
	 * @throws Exception 
	 */
	boolean existUnFetchUrlInAction(SpiderTaskPo task, String actionId) throws Exception;

	/**
	 * 设置url状态
	 * 
	 * @author zhujun
	 * @date 2016年7月8日
	 *
	 * @param task
	 * @param urlId
	 * @param status
	 * @param time
	 * @return 影响数据行数
	 * @throws Exception 
	 */
	int setFetchUrlStatus(SpiderTaskPo task, String urlId, int status, Date time) throws Exception;

	/**
	 * 保存抓取成功信息
	 *
	 * @param task
	 * @param urlId
	 * @param time
	 * @param httpStatusCode
	 * @return
	 */
	int saveFetchSuccessInfo(SpiderTaskPo task, String urlId, Date time, int httpStatusCode);
	
}
