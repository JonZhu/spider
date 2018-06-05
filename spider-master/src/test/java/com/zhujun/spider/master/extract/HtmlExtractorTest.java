package com.zhujun.spider.master.extract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HtmlExtractorTest {

    @Test
    public void extract() {
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

    @Test
    public void parseConfig() throws ScriptException {
        String config = "/htmlExtractConfig.js";
        InputStream configInputStream = getClass().getResourceAsStream(config);
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        Map result = (Map)engine.eval(new InputStreamReader(configInputStream, Charset.forName("UTF-8")));

        System.out.println(result.get("selector"));
    }

}