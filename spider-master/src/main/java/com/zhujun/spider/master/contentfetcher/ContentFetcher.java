package com.zhujun.spider.master.contentfetcher;

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
	byte[] fetch(String url);
	
}
