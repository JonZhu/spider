package com.zhujun.spider.master.schedule;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.zhujun.spider.master.domain.Url;
import com.zhujun.spider.net.url.ContentFetcher;
import com.zhujun.spider.net.url.IFetchResult;
import com.zhujun.spider.net.url.JavaUrlContentFetcher;
import org.springframework.stereotype.Component;

@Component
public class UrlExecutor implements ActionExecutor {

	@Override
	public void execute(IScheduleContext context) throws IOException {
		Url urlAction = (Url)context.getAction();
		Map<String, Serializable> dataScope = context.getDataScope();

		ContentFetcher contentFetcher = JavaUrlContentFetcher.getInstance();
		IFetchResult result = contentFetcher.fetch(urlAction.getHref());
		
		//content写入文件
		context.getDataWriter().write(urlAction.getHref(), result.getContentType(), new Date(), result.getData());
		
		if (StringUtils.isNotBlank(urlAction.getId())) {
			dataScope.put(urlAction.getId(), result.getData()); // content写入scope,供后面的action使用
		}
	}

}
