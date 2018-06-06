package com.zhujun.spider.master.extract;

import com.zhujun.spider.master.extract.html.ConfigParseUtil;
import com.zhujun.spider.master.extract.html.DataItemConfig;
import com.zhujun.spider.master.extract.html.HtmlExtractor;
import com.zhujun.spider.net.url.ContentFetcher;
import com.zhujun.spider.net.url.IFetchResult;
import com.zhujun.spider.net.url.JavaUrlContentFetcher;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HtmlExtractorTest {

    @Test
    public void extract() throws ScriptException, IOException {
        DataItemConfig config = parseConfig();
        HtmlExtractor extractor = new HtmlExtractor(config);
        String url = "https://baike.baidu.com/item/%E9%95%BF%E6%B1%9F%E4%B8%89%E8%A7%92%E6%B4%B2%E5%9F%8E%E5%B8%82%E7%BE%A4/5973620";
        ContentFetcher contentFetcher = JavaUrlContentFetcher.getInstance();
        IFetchResult fetchResult = contentFetcher.fetch(url);
        ExtractResult result = extractor.extract(url, fetchResult.getContentType(), fetchResult.getData());
        System.out.println(result);
    }

    @Test
    public void testParseBasicInfo() throws Exception {

        String url = "https://baike.baidu.com/item/%E9%95%BF%E6%B1%9F%E4%B8%89%E8%A7%92%E6%B4%B2%E5%9F%8E%E5%B8%82%E7%BE%A4/5973620";
        Document document = Jsoup.parse(new URL(url), 5000);
//        Element basicInfoDiv = document.select(".basic-info").first();
        Elements dtElements = document.select(".basic-info dt");
        Map<String, String> data = new HashMap<>();
        for (Element dtEle : dtElements) {
            Element valueEle = dtEle.nextElementSibling();
            data.put(dtEle.text(), valueEle == null ? null : valueEle.text());
        }

        System.out.println(data.toString());

    }


    public DataItemConfig parseConfig() throws ScriptException, IOException {
        String config = "/htmlExtractConfig.js";
        InputStream configInputStream = getClass().getResourceAsStream(config);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(configInputStream, byteArrayOutputStream);
        return ConfigParseUtil.parseJsConfig(new String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8")));
    }

}