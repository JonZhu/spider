package com.zhujun.spider.master.extract.html;

import com.zhujun.spider.master.extract.ExtractResult;
import com.zhujun.spider.master.extract.Extractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    private final DataItemConfig config;

    public HtmlExtractor(DataItemConfig config) {
        if (config == null) {
            throw new NullPointerException("config不能为空");
        }
        this.config = config;
    }


    @Override
    public ExtractResult extract(String url, String conentType, byte[] content) {
        Document document = Jsoup.parse(new String(content, Charset.forName("UTF-8")), url);
        Object data = extractCurrentConfig(document, document, this.config);
        ExtractResult result = new ExtractResult();
        result.setData(data);
        return result;
    }

    /**
     * 递归解析当前配置项
     * @param root
     * @param parent
     * @param currentConfig
     * @return
     */
    private Object extractCurrentConfig(Document root, Element parent, DataItemConfig currentConfig) {
        String dataType = currentConfig.getDataType();
        String selector = currentConfig.getSelector();

        if (DataItemConfig.DATA_TYPE_STRING.equalsIgnoreCase(dataType)) {
            // 解析string
            return extractString(root, parent, selector);
        } else if (DataItemConfig.DATA_TYPE_NUMBER.equalsIgnoreCase(dataType)) {
            return extractNumber(root, parent, selector);
        } else if (DataItemConfig.DATA_TYPE_DATE.equalsIgnoreCase(dataType)) {
            return extractDate(root, parent, selector, (DateDataConfig)currentConfig);
        } else if (DataItemConfig.DATA_TYPE_OBJECT.equalsIgnoreCase(dataType)) {
            return extractObject(root, parent, selector, (ObjectDataConfig)currentConfig);
        } else if (DataItemConfig.DATA_TYPE_ARRAY.equalsIgnoreCase(dataType)) {
            return extractArray(root, parent, selector, (ArrayDataConfig)currentConfig);
        }

        throw new RuntimeException("不支持的dateType: " + dataType);
    }

    private List extractArray(Document root, Element parent, String selector, ArrayDataConfig config) {
        return null;
    }

    private Map extractObject(Document root, Element parent, String selector, ObjectDataConfig config) {
        DataItemConfig[] propList = config.getProperties();
        if (propList == null || propList.length == 0) {
            return null;
        }

        Map objValue = new HashMap();
        Element propParentEle = StringUtils.isBlank(selector) ? parent : parent.select(selector).first();
        propParentEle = propParentEle == null ? parent : propParentEle;
        for (DataItemConfig propConfig : propList) {
            if (propConfig instanceof ReferenceDataConfig) {
                // 引用属性
                ReferenceDataConfig referenceDataConfig = (ReferenceDataConfig) propConfig;
                objValue.put(referenceDataConfig.getName(),
                        extractCurrentConfig(root, propParentEle, referenceDataConfig.getRef()));
            } else if (propConfig.getNameSelector() != null) {
                // 动态属性
                extractObjectDynamicProp(root, propParentEle, propConfig, objValue);
            } else if (propConfig.getName() != null) {
                // 静态属性
                objValue.put(propConfig.getName(), extractCurrentConfig(root, propParentEle, propConfig));
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
     * @param objValue 向该value中增加动态属性
     */
    private void extractObjectDynamicProp(Document root, Element parent, DataItemConfig propConfig, Map objValue) {
    }

    private String extractString(Document root, Element parent, String selector) {
        Elements selectElements = null;
        if (selector.startsWith("/")) {
            // 绝对路径
            selectElements = root.select(selector.replaceAll("^/+", ""));
        } else {
            // 相对路径
            selectElements = parent.select(selector);
        }
        return selectElements == null || selectElements.isEmpty() ? null : selectElements.text();
    }

    private Double extractNumber(Document root, Element parent, String selector) {
        String stringValue = parent.select(selector).text();
        return stringValue == null ? null : Double.parseDouble(stringValue);
    }

    private Date extractDate(Document root, Element parent, String selector, DateDataConfig config){
        String stringValue = parent.select(selector).text();
        try {
            return stringValue == null ? null : DateUtils.parseDate(stringValue, config.getDateFormat());
        } catch (ParseException e) {
            throw new RuntimeException("解析date出错", e);
        }
    }
}
