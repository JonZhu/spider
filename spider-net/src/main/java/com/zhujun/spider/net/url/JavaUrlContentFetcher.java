package com.zhujun.spider.net.url;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
	
	/**
	 * 连接超时, 5秒
	 */
	private final static int CONNECT_TIMEOUT = 5000;
	
	/**
	 * 读数据超时, 60秒
	 */
	private final static int READ_TIMEOUT = 60000;
	
	
	private final static String[] USER_AGENTS = new String[]{
			// firefox
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1) Gecko/20070803 Firefox/1.5.0.12",
			
			// chrome
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36",
			"Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.27 Safari/525.13",
			
			// ie
			"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)",
			
			// opera
			"Opera/9.27 (Windows NT 5.2; U; zh-cn)",
			"Mozilla/5.0 (Macintosh; PPC Mac OS X; U; en) Opera 8.0",
			
			// safari
			"Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13",
			"Mozilla/5.0 (iPhone; U; CPU like Mac OS X) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/4A93 Safari/419.3",
			
			
	};
	
	public static JavaUrlContentFetcher getInstance() {
		return INSTANCE;
	}
	
	@Override
	public IFetchResult fetch(String url) {
		
		InputStream urlInputStream = null;
		HttpURLConnection connection = null;
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("fetch: {}", url);
			}
			
			URL urlObj = new URL(processUrlString(url));
			connection = (HttpURLConnection)urlObj.openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT); //设置连接超时
			connection.setReadTimeout(READ_TIMEOUT); //设置读数据超时
			connection.setDoOutput(false);
			connection.setDoInput(true);
			
			connection.setRequestProperty("User-Agent", USER_AGENTS[RandomUtils.nextInt(0, USER_AGENTS.length)]);
			connection.setRequestProperty("Cache-Control", "max-age=0");
			connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			
			int httpRespCode = connection.getResponseCode();
			if (httpRespCode >= 400) {
				throw new Exception("http response code >= 400");
			}
			
			String contentType = connection.getHeaderField("Content-Type");
			urlInputStream = connection.getInputStream();
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			IOUtils.copy(urlInputStream, byteOutputStream);
			
			FetchResultImpl result = new FetchResultImpl();
			
			result.setContentType(contentType);
			result.setData(byteOutputStream.toByteArray());
			return result;
		} catch (Exception e) {
			throw new RuntimeException("fetch content fail", e);
		} finally {
			IOUtils.closeQuietly(urlInputStream);
			
			if (connection != null) {
				connection.disconnect();
			}
		}
		
	}

	/**
	 * 处理url
	 * @param url
	 * @return
	 */
	private String processUrlString(String url) {
		if (url == null) {
			return null;
		}

		return url.replace(" ", "%20"); // 处理空格
	}


}
