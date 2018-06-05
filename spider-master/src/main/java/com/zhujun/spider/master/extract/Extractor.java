package com.zhujun.spider.master.extract;

import java.util.Map;

/**
 * 数据抽取器
 *
 * @author zhujun
 * @desc Extractor
 * @time 2018/6/5 10:27
 */
public interface Extractor {

    /**
     * 数据抽取
     *
     * @param url
     * @param conentType
     * @param content
     * @return
     */
    Map extract(String url, String conentType, byte[] content);

}
