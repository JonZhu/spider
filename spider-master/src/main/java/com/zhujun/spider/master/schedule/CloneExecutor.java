package com.zhujun.spider.master.schedule;

import com.zhujun.spider.master.data.db.IFetchUrlService;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.writer.SpiderDataWriter;
import com.zhujun.spider.master.domain.Clone;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.schedule.PushDataQueue.Item;
import com.zhujun.spider.master.schedule.progress.IStep;
import com.zhujun.spider.master.schedule.progress.ProgressUtils;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class CloneExecutor implements ActionExecutor {

	private final static Logger LOG = LoggerFactory.getLogger(CloneExecutor.class);
	
	/**
	 * 可解析的页面content-type
	 */
	private final static String[] PAGE_CONTENT_TYPES = new String[]{
		"text/html"
	};

	@Autowired
	private IFetchUrlService fetchUrlService;
	
	@Override
	public void execute(IScheduleContext context) throws Exception {
		final Clone clone = (Clone)context.getAction();
		final String[] limits = clone.getHosts();
		
		List<IStep> stepList = new ArrayList<>();
		// seeds入库 step
		IStep insertSeedsStep = new IStep() {
			@Override
			public void execute(IScheduleContext c) throws Exception {
				for (String seed : clone.getSeeds()) {
					if (isUrlInHosts(seed, limits)) {
						FetchUrlPo fetchUrl = new FetchUrlPo();
						fetchUrl.setActionId(clone.getId());
						fetchUrl.setUrl(seed);
						fetchUrlService.createFetchUrl(c.getSpider().getDataDir(), fetchUrl);
						LOG.debug("seed入库: {}", seed);
					}
				}
				
			}
		};
		stepList.add(insertSeedsStep);
		
		
		// 处理数据 step
		IStep processDataStep = new IStep() {
			@Override
			public void execute(IScheduleContext c) throws Exception {
				SpiderDataWriter writer = c.getDataWriter();
				
				Item item = null;
				while (true) {
					item = ScheduleUtil.waitPushData(c.getSpider(), clone.getId(), fetchUrlService);
					if (item == null) {
						break;
					}
					
					if (item.success) {
						// 存储到文件
						writer.write(item.url, item.contentType, item.fetchTime, item.data);
						
						// 解析连接的url
						parseLinkedUrls(item.url, item.contentType, item.data, clone, c.getSpider());
						
						fetchUrlService.setFetchUrlStatus(c.getSpider().getDataDir(), item.urlId, FetchUrlPo.STATUS_SUCCESS, item.fetchTime);
					} else {
						// 抓取失败
						fetchUrlService.setFetchUrlStatus(c.getSpider().getDataDir(), item.urlId, FetchUrlPo.STATUS_ERROR, item.fetchTime);
					}
				}
			}
		};
		stepList.add(processDataStep);
		
		
		ProgressUtils.executeSteps(context, stepList, '_');
	}
	
	
	/**
	 * 解析页面中连接的资源url
	 * 
	 * @author zhujun
	 * @date 2016年7月18日
	 *
	 * @param url
	 * @param data
	 * @throws Exception 
	 */
	protected void parseLinkedUrls(String url, String contentType, byte[] data, Clone clone, Spider spider) throws Exception {
		if (!pageCanParsed(contentType)) {
			return;
		}
		
		long startTime = System.currentTimeMillis();
		Document doc = null;
		try {
			doc = Jsoup.parse(new String(data, "UTF-8"), url);
		} catch (Exception e) {
			// html解析失败
			return;
		}
		
		List<FetchUrlPo> urlPoList = new ArrayList<>();
		
		// 查询a标签
		Elements aEles = doc.select("a[href]");
		String href = null;
		if (aEles != null && !aEles.isEmpty()) {
			for (Element aEle : aEles) {
				href = StringUtils.trim(aEle.attr("href"));
				if (StringUtils.isNotBlank(href) && !href.startsWith("#") && !href.startsWith("javascript:")) {
					String absoluteUrl = UrlUtils.buildAbsoluteUrl(url, href);
					if (isUrlInHosts(absoluteUrl, clone.getHosts())) {
						FetchUrlPo fetchUrl = new FetchUrlPo();
						fetchUrl.setActionId(clone.getId());
						fetchUrl.setUrl(absoluteUrl);
						urlPoList.add(fetchUrl);
//						LOG.debug("增加关联url: {}", absoluteUrl);
						
						if (urlPoList.size() > 1000) {
							fetchUrlService.createFetchUrl(spider.getDataDir(), urlPoList);
							urlPoList = new ArrayList<>();
						}
					}
				}
			}
		}
		
		if (clone.isAllowCss()) {
			// css
			Elements linkEles = doc.select("link[href]");
			if (linkEles != null && !linkEles.isEmpty()) {
				for (Element linkEle : linkEles) {
					String absUrl = linkEle.absUrl("href");
					FetchUrlPo fetchUrl = new FetchUrlPo();
					fetchUrl.setActionId(clone.getId());
					fetchUrl.setUrl(absUrl);
					urlPoList.add(fetchUrl);
//					LOG.debug("增加关联css url: {}", absUrl);
					
					if (urlPoList.size() > 1000) {
						fetchUrlService.createFetchUrl(spider.getDataDir(), urlPoList);
						urlPoList = new ArrayList<>();
					}
				}
				
			}
		}
		
		if (clone.isAllowJs()) {
			// js
			Elements jsEles = doc.select("script[src]");
			if (jsEles != null && !jsEles.isEmpty()) {
				for (Element linkEle : jsEles) {
					String absUrl = linkEle.absUrl("src");
					FetchUrlPo fetchUrl = new FetchUrlPo();
					fetchUrl.setActionId(clone.getId());
					fetchUrl.setUrl(absUrl);
					urlPoList.add(fetchUrl);
//					LOG.debug("增加关联js url: {}", absUrl);
					
					if (urlPoList.size() > 1000) {
						fetchUrlService.createFetchUrl(spider.getDataDir(), urlPoList);
						urlPoList = new ArrayList<>();
					}
				}
				
			}
		}
		
		if (clone.isAllowImage()) {
			// js
			Elements imageEles = doc.select("img[src]");
			if (imageEles != null && !imageEles.isEmpty()) {
				for (Element imageEle : imageEles) {
					String imageUrl = imageEle.absUrl("src");
					FetchUrlPo fetchUrl = new FetchUrlPo();
					fetchUrl.setActionId(clone.getId());
					fetchUrl.setUrl(imageUrl);
					urlPoList.add(fetchUrl);
//					LOG.debug("增加关联image url: {}", imageUrl);
					
					if (urlPoList.size() > 1000) {
						fetchUrlService.createFetchUrl(spider.getDataDir(), urlPoList);
						urlPoList = new ArrayList<>();
					}
				}
				
			}
		}
		
		if (urlPoList.size() > 0) {
			fetchUrlService.createFetchUrl(spider.getDataDir(), urlPoList);
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("parse link url and insert into database cost {} ms", System.currentTimeMillis() - startTime);
		}
		
	}


	/**
	 * 判断是否可解析
	 * 
	 * @author zhujun
	 * @date 2016年7月18日
	 *
	 * @param contentType
	 * @return
	 */
	private boolean pageCanParsed(String contentType) {
		for (String type : PAGE_CONTENT_TYPES) {
			if (contentType.indexOf(type) > -1) {
				return true;
			}
		}
		return false;
	}


	private boolean isUrlInHosts(String url, String[] hosts) {
		URL urlObj = null;
		try {
			urlObj = new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
		
		String urlHost = urlObj.getHost();
		for (String host : hosts) {
			if (urlHost.endsWith(host)) {
				return true;
			}
		}
		
		return false;
	}
	
}
