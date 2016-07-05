package com.zhujun.spider.master.data.db;

import java.util.List;

import com.zhujun.spider.master.data.db.po.FetchUrlPo;

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
	 * @param dataDir
	 * @param fetchUrl
	 * @return
	 * @throws Exception 
	 */
	void createFetchUrl(String dataDir, FetchUrlPo fetchUrl) throws Exception;
	
	/**
	 * 批量创建url
	 * 
	 * @author zhujun
	 * @date 2016年7月5日
	 *
	 * @param dataDir
	 * @param fetchUrlList
	 * @throws Exception
	 */
	void createFetchUrl(String dataDir, List<FetchUrlPo> fetchUrlList) throws Exception;
	
	/**
	 * 获取要分发的url
	 * 
	 * @author zhujun
	 * @date 2016年7月4日
	 *
	 * @param dataDir
	 * @return
	 * @throws Exception 
	 */
	List<FetchUrlPo> getGiveOutUrls(String dataDir) throws Exception;
	
}
