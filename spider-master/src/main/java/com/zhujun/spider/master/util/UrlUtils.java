package com.zhujun.spider.master.util;

/**
 * Url工具
 * 
 * @author zhujun
 * @date 2016年7月18日
 *
 */
public class UrlUtils {

	/**
	 * 构建绝对url
	 * 
	 * @author zhujun
	 * @date 2016年6月7日
	 *
	 * @param baseUrl
	 * @param url
	 * @return
	 */
	public static String buildAbsoluteUrl(String baseUrl, String url) {
		if (url.startsWith("/")) {
			// 相对于baseUrl的根路径
			return baseUrl.substring(0, baseUrl.indexOf("/", 8)) + url;
		} else if (url.startsWith("http://") || url.startsWith("https://")) {
			// 已经是绝对url
			return url;
		} else {
			// 相对于baseUrl的当前路径
			return baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1) + url;
		}
	}
	
}
