package com.zhujun.spider.master.contentfetcher;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用java url实现
 * 
 * @author zhujun
 * @date 2016年6月4日
 *
 */
public class JavaUrlContentFetcher implements ContentFetcher {

	private final static Logger LOG = LoggerFactory.getLogger(JavaUrlContentFetcher.class);
	
	private final static JavaUrlContentFetcher INSTANCE = new JavaUrlContentFetcher();
	
	public static JavaUrlContentFetcher getInstance() {
		return INSTANCE;
	}
	
	@Override
	public byte[] fetch(String url) {
		
		InputStream urlInputStream = null;
		HttpURLConnection connection = null;
		try {
			URL urlObj = new URL(url);
			connection = (HttpURLConnection)urlObj.openConnection();
			connection.setDoOutput(false);
			connection.setDoInput(true);
			
			connection.setRequestProperty("Cache-Control", "max-age=0");
			connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0");
			connection.setRequestProperty("Cookie", "uuid=9e092386c0fa758890e2.1463734290.0.0.0; oc=0FqDK5dyzNwN25WVxCYnLCgbTeVLDCTLXBprl8aFq2L5Ec5sNCr3fB4E9Ze78ds97zMKSVz3XjiV89KzpzD4G--iuuta2o8i4X0ORQzuftqDE2rQuVW2_768oZ9-lMUJae6DnHBZ_45nl5Qa6QLEmzKZo8f0kLY6qr_jVrDYbyI; ci=59; abt=1467952750.0%7CBCF; rvct=59%2C151; __utma=211559370.391048430.1463734294.1467267264.1467952622.10; __utmz=211559370.1463734294.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utmv=211559370.|1=city=cd=1^3=dealtype=26=1^5=cate=all=1; __mta=113687224.1463734304058.1467268110576.1467952621501.45; rvd=33071639%2C32738810%2C33071643%2C29272888%2C32946448; __utmb=211559370.4.10.1467952622; __utmc=211559370; SID=2rd75p4hgfg1dtimcodn05agg7");
			
			int httpRespCode = connection.getResponseCode();
			if (httpRespCode >= 400) {
				throw new Exception("http response code >= 400");
			}
			
			urlInputStream = connection.getInputStream();
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			IOUtils.copy(urlInputStream, byteOutputStream);
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("fetch: {}", url);
			}
			
			return byteOutputStream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("获取内容失败", e);
		} finally {
			IOUtils.closeQuietly(urlInputStream);
			
			if (connection != null) {
				connection.disconnect();
			}
		}
		
	}
	

}
