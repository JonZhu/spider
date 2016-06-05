package com.zhujun.spider.master.schedule;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.zhujun.spider.master.contentfetcher.ContentFetcher;
import com.zhujun.spider.master.contentfetcher.JavaUrlContentFetcher;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.UrlSet;

public class UrlSetExecutor implements ActionExecutor {

	@Override
	public void execute(Spider spider, DslAction action, Map<String, Object> dataScope) {
		UrlSet urlSet = (UrlSet)action;
		
		ContentFetcher contentFetcher = JavaUrlContentFetcher.getInstance();
		
		
		List<Integer> indexList = urlSet.getTempIndexList();
		if (indexList == null || indexList.isEmpty()) {
			// 无模板号
			byte[] content = contentFetcher.fetch(urlSet.getUrltemplate());
			//TODO 存储到文件
			
			if (StringUtils.isNotBlank(urlSet.getName())) {
				dataScope.put(urlSet.getName(), content);
			}
		} else {
			// 填充模板值
			
			
		}
		
		

	}

}
