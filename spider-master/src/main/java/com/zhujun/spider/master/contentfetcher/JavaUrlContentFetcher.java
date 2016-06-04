package com.zhujun.spider.master.contentfetcher;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * 使用java url实现
 * 
 * @author zhujun
 * @date 2016年6月4日
 *
 */
public class JavaUrlContentFetcher implements ContentFetcher {

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
			
			urlInputStream = connection.getInputStream();
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			IOUtils.copy(urlInputStream, byteOutputStream);
			
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
