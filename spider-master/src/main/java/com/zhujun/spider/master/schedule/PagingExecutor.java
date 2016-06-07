package com.zhujun.spider.master.schedule;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.Paging;
import com.zhujun.spider.master.domain.Spider;

public class PagingExecutor implements ActionExecutor {

	@Override
	public void execute(Spider spider, DslAction action, Map<String, Object> dataScope) {
		Paging paging = (Paging)action;

		String currentPageUrl = (String)dataScope.get(ScheduleConst.PRE_RESULT_URL_KEY); // 数据页url
		String currentPage = ScheduleUtil.obj2str(dataScope.get(ScheduleConst.PRE_RESULT_DATA_KEY)); // 获取数据
		Document doc = Jsoup.parse(currentPage, currentPageUrl);
		
		Elements selectedEles = doc.select(paging.getSelect());
		if (selectedEles == null && selectedEles.isEmpty()) {
			return;
		}
		
		for (Element element : selectedEles) {
			String url = element.attr(paging.getUrlAttr());
			if (StringUtils.isNotBlank(url)) {
				String pagingUrl = buildAbsoluteUrl(currentPageUrl, url);
				addUrl2fetchQueue(pagingUrl);
			}
		}
	}
	

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
	private String buildAbsoluteUrl(String baseUrl, String url) {
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

	private void addUrl2fetchQueue(String url) {
		// TODO Auto-generated method stub
		
	}

}
