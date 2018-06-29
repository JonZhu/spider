package com.zhujun.spider.net.url;

import com.zhujun.spider.master.domain.Url;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class JavaUrlContentFetcherTest {

    @Test
    public void fetch() {
        String url = "http://baike.baidu.com/item/Muriel Robin/18789112";
        url = url.replace(" ", "%20");
        IFetchResult result = JavaUrlContentFetcher.getInstance().fetch(url);
        System.out.println(result.getContentType());
    }

    @Test
    public void testEncodeUrl() throws MalformedURLException, UnsupportedEncodingException {
        String urlString = "http://baike.baidu.com/item/Muriel Robin/18789112?p=323#dsafas";
        System.out.println(URLEncoder.encode(urlString, "UTF-8"));

        URL url = new URL(urlString);

        System.out.println(url.getPath());
    }
}