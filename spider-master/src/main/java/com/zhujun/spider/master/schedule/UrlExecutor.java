package com.zhujun.spider.master.schedule;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.zhujun.spider.master.contentfetcher.ContentFetcher;
import com.zhujun.spider.master.contentfetcher.JavaUrlContentFetcher;
import com.zhujun.spider.master.domain.Url;

public class UrlExecutor implements ActionExecutor {

	
//	private IFetchUrlService fetchUrlService = DIContext.getInstance(IFetchUrlService.class);
	
	
	@Override
	public void execute(IScheduleContext context) {
		Url urlAction = (Url)context.getAction();
		Map<String, Serializable> dataScope = context.getDataScope();

		ContentFetcher contentFetcher = JavaUrlContentFetcher.getInstance();
		byte[] content = contentFetcher.fetch(urlAction.getHref());
		
		//content写入文件
		context.getDataWriter().write(urlAction.getHref(), new Date(), content);
		
		if (StringUtils.isNotBlank(urlAction.getId())) {
			dataScope.put(urlAction.getId(), content); // content写入scope,供后面的action使用
		}
	}

}
