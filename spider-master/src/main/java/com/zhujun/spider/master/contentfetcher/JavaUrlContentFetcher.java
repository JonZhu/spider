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
			connection.disconnect();
			connection.setDoOutput(false);
			connection.setDoInput(true);
			
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
