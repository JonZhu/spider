package com.zhujun.spider.master.schedule;

import com.zhujun.spider.master.data.db.IFetchUrlService;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.domain.Paging;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.util.UrlUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
public class PagingExecutor implements ActionExecutor {

	private final static Logger LOG = LoggerFactory.getLogger(PagingExecutor.class);

	@Autowired
	private IFetchUrlService fetchUrlService;
	
	@Override
	public void execute(IScheduleContext context) throws Exception {
		Spider spider = context.getSpider();
		Paging paging = (Paging)context.getAction();
		Map<String, Serializable> dataScope = context.getDataScope();
		
		String currentPageUrl = (String)dataScope.get(ScheduleConst.PRE_RESULT_URL_KEY); // 数据页url
		String currentPage = ScheduleUtil.obj2str(dataScope.get(ScheduleConst.PRE_RESULT_DATA_KEY)); // 获取数据
		Document doc = Jsoup.parse(currentPage, currentPageUrl);
		
		Elements selectedEles = doc.select(paging.getSelect());
		if (selectedEles == null || selectedEles.isEmpty()) {
			return;
		}
		
		for (Element element : selectedEles) {
			String url = element.attr(paging.getUrlAttr());
			if (StringUtils.isNotBlank(url)) {
				String pagingUrl = UrlUtils.buildAbsoluteUrl(currentPageUrl, url);
				
				FetchUrlPo fetchUrl = new FetchUrlPo();
				fetchUrl.setUrl(pagingUrl);
				fetchUrl.setActionId(context.getParentAction().getId());
				fetchUrlService.createFetchUrl(context.getSpiderTaskPo(), fetchUrl);
				LOG.debug(pagingUrl);
			}
		}
	}

}
