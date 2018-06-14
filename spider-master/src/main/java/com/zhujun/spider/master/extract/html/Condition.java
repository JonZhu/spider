package com.zhujun.spider.master.extract.html;

import java.util.regex.Pattern;

/**
 * 数据抽取条件
 *
 * @author zhujun
 * @desc Condition
 * @time 2018/6/14 17:04
 */
public class Condition {

    /**
     * 结点选择器, 依赖于element存在
     */
    private String elementSelector;

    /**
     * url正则表达式, 依赖于url正则表达式模式
     */
    private Pattern urlPatter;

    public String getElementSelector() {
        return elementSelector;
    }

    public void setElementSelector(String elementSelector) {
        this.elementSelector = elementSelector;
    }

    public Pattern getUrlPatter() {
        return urlPatter;
    }

    public void setUrlPatter(Pattern urlPatter) {
        this.urlPatter = urlPatter;
    }
}
