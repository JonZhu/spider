package com.zhujun.spider.master.extract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * html数据抽取
 *
 * @author zhujun
 * @desc HtmlExtractor
 * @time 2018/6/5 10:34
 */
public class HtmlExtractor implements Extractor {
    @Override
    public Map extract(String url, String conentType, byte[] content) {
        Document document = Jsoup.parse(new String(content, Charset.forName("UTF-8")), url);
        return null;
    }
}
