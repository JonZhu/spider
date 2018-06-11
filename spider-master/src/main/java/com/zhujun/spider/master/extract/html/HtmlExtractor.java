package com.zhujun.spider.master.extract.html;

import com.zhujun.spider.master.extract.ExtractResult;
import com.zhujun.spider.master.extract.Extractor;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * html数据抽取
 *
 * @author zhujun
 * @desc HtmlExtractor
 * @time 2018/6/5 10:34
 */
public class HtmlExtractor implements Extractor {

    private final static Logger log = LoggerFactory.getLogger(HtmlExtractor.class);

    /**
     * 扩展 函数模式
     * group(1) 函数声明
     * group(2) 函数名
     * group(3) 参数
     */
    private final static Pattern EXT_DEFINE_FUN = Pattern.compile(":((next-one|pre-one)\\(([^()]*)\\))");

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
        Elements itemElements = jsoupSelect(root, parent, selector);
        if (itemElements == null || itemElements.isEmpty()) {
            return null;
        }

        DataItemConfig itemConfig = config.getItemData();
        if (itemConfig == null) {
            throw new RuntimeException("数组itemData不能为空");
        }

        List arrayData = new ArrayList();
        int startSkip = config.getStartSkip() == null ? 0 : config.getStartSkip();
        int endSkip = config.getEndSkip() == null ? 0 : config.getEndSkip();
        for (int i = startSkip; i < itemElements.size() - endSkip; i++) {
            Element itemEle = itemElements.get(i);
            arrayData.add(extractCurrentConfig(root, itemEle, itemConfig));
        }
        return arrayData;
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
        // 搜索prop name结点
        Elements nameElements = jsoupSelect(root, parent, propConfig.getNameSelector());
        if (nameElements == null || nameElements.isEmpty()) {
            return;
        }

        for (Element nameEle : nameElements) {
            objValue.put(nameEle.text(), extractObjectDynamicPropValue(root, parent, nameEle, propConfig));
        }
    }

    private Object extractObjectDynamicPropValue(Document root, Element parent, Element nameEle, DataItemConfig propConfig) {
        String valueSelector = propConfig.getSelector();
        if (valueSelector.startsWith("$name")) {
            // 相对于name selector搜索
            AbstractDataItemConfig newConfig = (AbstractDataItemConfig)ObjectUtils.clone(propConfig);
            newConfig.setSelector(valueSelector.substring(5)); // 去除扩展元素$name
            return extractCurrentConfig(root, nameEle, newConfig);
        } else {
            return extractCurrentConfig(root, parent, propConfig);
        }
    }

    /**
     * jsoup搜索
     *
     * <p>支持绝对路径和相对路径</p>
     *
     * @param root
     * @param parent
     * @param selector
     * @return
     */
    private Elements jsoupSelect(Document root, Element parent, String selector) {
        if (StringUtils.isBlank(selector)) {
            // selector为空,直接返回父级
            return new Elements(parent);
        }

        Element searchEle = null;
        String currentSelector = null;
        if (selector.startsWith("/")) {
            // 绝对路径
            searchEle = root;
            currentSelector = selector.replaceAll("^/+", "");
        } else {
            // 相对路径
            searchEle = parent;
            currentSelector = selector;
            while (true) {
                if (currentSelector.startsWith("../")) {
                    // 父级导航
                    searchEle = searchEle.parent();
                    currentSelector = currentSelector.substring(3);
                } else {
                    break;
                }
            }
        }

        Elements currentResults = new Elements(searchEle); // 当前中间结果
        // 自定义函数处理
        Matcher matcher = EXT_DEFINE_FUN.matcher(currentSelector);
        while (currentResults != null && !currentResults.isEmpty() && matcher.find()) {
            // 查询到扩展函数
            String funName = matcher.group(2);
            String funArgu = matcher.group(3);

            String preSelector = currentSelector.substring(0, matcher.start());
            if (StringUtils.isNotBlank(preSelector)) {
                // 执行函数前的结果
                currentResults = currentResults.select(preSelector);
            }

            // 执行函数
            currentResults = executeExtFunction(currentResults, funName, funArgu);

            // 去除已执行表达式
            currentSelector = currentSelector.substring(matcher.end());
        }

        if (currentResults != null && !currentResults.isEmpty() && StringUtils.isNoneBlank(currentSelector)) {
            // 执行剩余selector
            currentResults = currentResults.select(currentSelector);
        }

        return currentResults;
    }

    /**
     * 执行扩展函数
     *
     * @param elements 源数据结点
     * @param funName 函数名
     * @param funArgu 函数值
     * @return
     */
    private Elements executeExtFunction(Elements elements, String funName, String funArgu) {
        if ("next-one".equalsIgnoreCase(funName)) {
            return executeNextOneFunction(elements, funArgu);
        }

        throw new RuntimeException("不支持的扩展函数: " + funName);
    }

    /**
     * 执行 next-one 函数
     * @param elements
     * @param funArgu
     * @return
     */
    private Elements executeNextOneFunction(Elements elements, String funArgu) {
        boolean hasArgu = StringUtils.isNotBlank(funArgu); // 是否包含参数
        for (Element element : elements) {
            Element eleParent = element.parent();
            if (eleParent == null) {
                continue;
            }

            List<Element> siblingList = eleParent.children(); // 兄弟节点
            int eleIndex = siblingList.indexOf(element); // 查询节点位置
            if (eleIndex < siblingList.size() - 1) {
                for (int i = eleIndex + 1; i < siblingList.size(); i++) {
                    Element nextEle = siblingList.get(i); // 后续兄弟节点
                    if (!hasArgu) {
                        // 没有参数
                        return new Elements(nextEle);
                    } else {
                        // 使用参数搜索
                        Elements arguSelectResult = jsoupSelect(null, nextEle, funArgu);
                        if (arguSelectResult != null && !arguSelectResult.isEmpty()) {
                            return new Elements(arguSelectResult.first()); // 返回第一个
                        }
                    }
                }
            }
        }

        return null;
    }

    private String extractString(Document root, Element parent, String selector) {
        Elements selectElements = jsoupSelect(root, parent, selector);
        return selectElements == null || selectElements.isEmpty() ? null : selectElements.text();
    }

    private Double extractNumber(Document root, Element parent, String selector) {
        String stringValue = extractString(root, parent, selector);
        return stringValue == null ? null : Double.parseDouble(stringValue);
    }

    private Date extractDate(Document root, Element parent, String selector, DateDataConfig config){
        String stringValue = extractString(root, parent, selector);
        try {
            return stringValue == null ? null : DateUtils.parseDate(stringValue, config.getDateFormat());
        } catch (ParseException e) {
            throw new RuntimeException("解析date出错", e);
        }
    }
}
