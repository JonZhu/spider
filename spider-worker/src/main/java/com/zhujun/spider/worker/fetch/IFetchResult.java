package com.zhujun.spider.worker.fetch;

/**
 * 抓取结果
 * 
 * @author zhujun
 * @date 2016年7月18日
 *
 */
public interface IFetchResult {

	String getContentType();
	
	byte[] getData();
	
}
