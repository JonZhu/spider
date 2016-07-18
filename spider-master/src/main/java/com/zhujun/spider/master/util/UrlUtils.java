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
			int index = baseUrl.indexOf("/", 8); // 查找http(s):// 之后的/
			return index > 0 ? baseUrl.substring(0, index) + url : baseUrl + url;
		} else if (url.startsWith("http://") || url.startsWith("https://")) {
			// 已经是绝对url
			return url;
		} else {
			// 相对于baseUrl的当前路径
			int index = baseUrl.lastIndexOf("/");
			if (index < 8) {
				return baseUrl + "/" + url;
			} else {
				return baseUrl.substring(0, index + 1) + url;
			}
		}
	}
	
}
