package com.zhujun.spider.master.schedule;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.zhujun.spider.master.contentfetcher.ContentFetcher;
import com.zhujun.spider.master.contentfetcher.JavaUrlContentFetcher;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.Url;

public class UrlExecutor implements ActionExecutor {

	
	
	@Override
	public void execute(Spider spider, DslAction action, Map<String, Object> dataScope) {
		Url urlAction = (Url)action;

		ContentFetcher contentFetcher = JavaUrlContentFetcher.getInstance();
		byte[] content = contentFetcher.fetch(urlAction.getHref());
		
		//TODO content写入文件
		
		if (StringUtils.isNotBlank(urlAction.getName())) {
			dataScope.put(urlAction.getName(), content); // content写入scope,供后面的action使用
		}
	}

}
