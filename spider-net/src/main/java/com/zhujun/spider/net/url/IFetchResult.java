package com.zhujun.spider.net.url;

import java.util.Map;

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

	int getHttpStatusCode();

	Map<String, String> getHeaders();
	
}
