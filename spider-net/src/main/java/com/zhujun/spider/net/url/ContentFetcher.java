package com.zhujun.spider.net.url;

/**
 * 内容获取
 * 
 * @author zhujun
 * @date 2016年6月4日
 *
 */
public interface ContentFetcher {

	/**
	 * 获取内容
	 * @param url
	 * @return
	 */
	IFetchResult fetch(String url);
	
}
