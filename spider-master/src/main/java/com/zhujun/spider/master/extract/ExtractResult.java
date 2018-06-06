package com.zhujun.spider.master.extract;

import java.util.Map;

/**
 * 抽取数据结果
 *
 * @author zhujun
 * @desc ExtractResult
 * @time 2018/6/6 10:41
 */
public class ExtractResult {
    /**
     * 抽取结果数据
     */
    private Object data;

    /**
     * 错误信息
     */
    private Map<String, String> errorInfo;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, String> getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(Map<String, String> errorInfo) {
        this.errorInfo = errorInfo;
    }
}
