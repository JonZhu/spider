package com.zhujun.spider.master.extract;

import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * html数据抽取
 *
 * @author zhujun
 * @desc HtmlExtractor
 * @time 2018/6/5 10:34
 */
public class HtmlExtractor implements Extractor {

    private final static Logger log = LoggerFactory.getLogger(HtmlExtractor.class);

    private final static String DATA_TYPE_STRING = "string";
    private final static String DATA_TYPE_NUMBER = "number";
    private final static String DATA_TYPE_DATE = "date";
    private final static String DATA_TYPE_OBJECT = "object";
    private final static String DATA_TYPE_ARRAY = "array";

    private final Map config;

    public HtmlExtractor(Map config) {
        if (config == null) {
            throw new NullPointerException("config不能为空");
        }
        this.config = config;
    }


    @Override
    public Map extract(String url, String conentType, byte[] content) {
        Document document = Jsoup.parse(new String(content, Charset.forName("UTF-8")), url);

        return (Map)extractCurrentConfig(document, document, this.config);
    }

    /**
     * 递归解析当前配置项
     * @param root
     * @param parent
     * @param currentConfig
     * @return
     */
    private Object extractCurrentConfig(Document root, Element parent, Map currentConfig) {
        String dataType = (String)currentConfig.get("dataType");
        String selector = (String)currentConfig.get("selector");

        if (DATA_TYPE_STRING.equalsIgnoreCase(dataType)) {
            // 解析string
            return extractString(root, parent, selector);
        } else if (DATA_TYPE_NUMBER.equalsIgnoreCase(dataType)) {
            return extractNumber(root, parent, selector);
        } else if (DATA_TYPE_DATE.equalsIgnoreCase(dataType)) {
            String dateFormat  = (String)currentConfig.get("dateFormat");
            return extractDate(root, parent, selector, dateFormat);
        } else if (DATA_TYPE_OBJECT.equalsIgnoreCase(dataType)) {
            return extractObject(root, parent, selector, currentConfig);
        } else if (DATA_TYPE_ARRAY.equalsIgnoreCase(dataType)) {
            return extractArray(root, parent, selector, currentConfig);
        }

        throw new RuntimeException("不支持的dateType: " + dataType);
    }

    private List extractArray(Document root, Element parent, String selector, Map currentConfig) {
        return null;
    }

    private Map extractObject(Document root, Element parent, String selector, Map currentConfig) {
        List<Map> propList = (List<Map>)currentConfig.get("properties");
        if (propList == null || propList.isEmpty()) {
            return null;
        }

        Map objValue = new HashMap();
        Element propParentEle = selector == null ? parent : parent.select(selector).first();
        propParentEle = propParentEle == null ? parent : propParentEle;
        for (Map propConfig : propList) {
            Object name = propConfig.get("name");
            if (name instanceof String) {
                // 静态属性
                objValue.put(name, extractCurrentConfig(root, propParentEle, propConfig));
            } else if (name instanceof Map) {
                // 动态属性
                extractObjectDynamicProp(root, propParentEle, propConfig, (Map)name, objValue);
            } else {
                log.warn("未指定object property的name配置属性");
            }
        }

        return objValue;
    }

    /**
     * 解析对象动态属性
     *
     * @param root 根结点
     * @param parent 父级结点
     * @param propConfig 属性配置
     * @param nameConfig 名称配置
     * @param objValue 向该value中增加动态属性
     */
    private void extractObjectDynamicProp(Document root, Element parent, Map propConfig, Map nameConfig, Map objValue) {
    }

    private String extractString(Document root, Element parent, String selector) {
        return parent.select(selector).text();
    }

    private Double extractNumber(Document root, Element parent, String selector) {
        String stringValue = parent.select(selector).text();
        return stringValue == null ? null : Double.parseDouble(stringValue);
    }

    private Date extractDate(Document root, Element parent, String selector, String dateFormat){
        String stringValue = parent.select(selector).text();
        try {
            return stringValue == null ? null : DateUtils.parseDate(stringValue, dateFormat);
        } catch (ParseException e) {
            throw new RuntimeException("解析date出错", e);
        }
    }
}
