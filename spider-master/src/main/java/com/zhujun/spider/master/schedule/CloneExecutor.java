package com.zhujun.spider.master.schedule;

import com.mongodb.MongoWriteException;
import com.zhujun.spider.master.data.db.IFetchUrlService;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.data.writer.SpiderDataWriter;
import com.zhujun.spider.master.domain.Clone;
import com.zhujun.spider.master.exception.ExceptionIgnore;
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
import java.util.regex.Pattern;

@Component
public class CloneExecutor implements ActionExecutor {

	private final static Logger LOG = LoggerFactory.getLogger(CloneExecutor.class);

	private final static CreateFetchUrlExceptionIgnore CREATE_FETCH_URL_EXCEPTION_IGNORE = new CreateFetchUrlExceptionIgnore();

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
						fetchUrlService.createFetchUrl(c.getSpiderTaskPo(), fetchUrl);
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
					item = ScheduleUtil.waitPushData(c.getSpiderTaskPo(), clone.getId(), fetchUrlService);
					if (item == null) {
						break;
					}
					
					if (item.success) {
						// 存储到文件
						writer.write(item.url, item.contentType, item.fetchTime, item.data);
						
						// 解析连接的url
						parseLinkedUrls(item.url, item.contentType, item.data, clone, c.getSpiderTaskPo());
						
						fetchUrlService.setFetchUrlStatus(c.getSpiderTaskPo(), item.urlId, FetchUrlPo.STATUS_SUCCESS, item.fetchTime);
					} else {
						// 抓取失败
						fetchUrlService.setFetchUrlStatus(c.getSpiderTaskPo(), item.urlId, FetchUrlPo.STATUS_ERROR, item.fetchTime);
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
	protected void parseLinkedUrls(String url, String contentType, byte[] data, Clone clone, SpiderTaskPo spider) throws Exception {
		if (!pageCanParsed(contentType)) {
			return;
		}
		
		long startTime = System.currentTimeMillis();
		Document doc = null;
		try {
			doc = Jsoup.parse(new String(data, "UTF-8"), url);
		} catch (Exception e) {
			// html解析失败
			LOG.error("解析html失败", e);
			return;
		}
		
		List<FetchUrlPo> urlPoList = new ArrayList<>();
		int paseUrlCount = 0;
		
		// 查询a标签
		Elements aEles = doc.select("a[href]");
		String href = null;
		if (aEles != null && !aEles.isEmpty()) {
			for (Element aEle : aEles) {
				href = StringUtils.trim(aEle.attr("href"));
				if (StringUtils.isNotBlank(href) && !href.startsWith("#") && !href.startsWith("javascript:")) {
					String absoluteUrl = UrlUtils.buildAbsoluteUrl(url, href);
					if (isUrlInHosts(absoluteUrl, clone.getHosts()) && matchPattern(absoluteUrl, clone.getUrlPatterns())) {
						FetchUrlPo fetchUrl = new FetchUrlPo();
						fetchUrl.setActionId(clone.getId());
						fetchUrl.setUrl(absoluteUrl);
						urlPoList.add(fetchUrl);
                        paseUrlCount++;
//						LOG.debug("增加关联url: {}", absoluteUrl);
						
						if (urlPoList.size() > 1000) {
							fetchUrlService.createFetchUrl(spider, urlPoList, CREATE_FETCH_URL_EXCEPTION_IGNORE);
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
                    paseUrlCount++;
//					LOG.debug("增加关联css url: {}", absUrl);
					
					if (urlPoList.size() > 1000) {
						fetchUrlService.createFetchUrl(spider, urlPoList, CREATE_FETCH_URL_EXCEPTION_IGNORE);
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
                    paseUrlCount++;
//					LOG.debug("增加关联js url: {}", absUrl);
					
					if (urlPoList.size() > 1000) {
						fetchUrlService.createFetchUrl(spider, urlPoList, CREATE_FETCH_URL_EXCEPTION_IGNORE);
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
                    paseUrlCount++;
//					LOG.debug("增加关联image url: {}", imageUrl);
					
					if (urlPoList.size() > 1000) {
						fetchUrlService.createFetchUrl(spider, urlPoList, CREATE_FETCH_URL_EXCEPTION_IGNORE);
						urlPoList = new ArrayList<>();
					}
				}
				
			}
		}
		
		if (urlPoList.size() > 0) {
			fetchUrlService.createFetchUrl(spider, urlPoList, CREATE_FETCH_URL_EXCEPTION_IGNORE);
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("parse {} link url and insert into database cost {} ms", paseUrlCount, System.currentTimeMillis() - startTime);
		}
		
	}

	private static class CreateFetchUrlExceptionIgnore implements ExceptionIgnore {
		@Override
		public boolean isIgnore(Exception e) {
			Throwable throwable = e;
			while (true) {
				if (throwable == null) {
					break;
				}
				if (throwable instanceof MongoWriteException) {
					if (((MongoWriteException) throwable).getError().getCode() == 1047) {
						// com.mongodb.MongoWriteException: WiredTigerIndex::insert: key too large to index, failing  1047
						return true;
					}
				}

				throwable = throwable.getCause(); // 遍历cause链
			}

			return false;
		}
	}

	/**
	 * 判断是否匹配模式
	 * @param absoluteUrl
	 * @param patterns
	 * @return
	 */
	private boolean matchPattern(String absoluteUrl, Pattern[] patterns) {
		if (patterns == null || patterns.length == 0) {
			return true;
		}

		if (absoluteUrl == null) {
			return false;
		}

		for (Pattern pattern : patterns) {
			if (pattern.matcher(absoluteUrl).find()) { // 使用find，能找到就行，不用完全匹配
				return true;
			}
		}

		// 不能匹配
		return false;
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
