package com.zhujun.spider.master.extract.html;

/**
 * 时间 数据配置
 *
 * @author zhujun
 * @desc DateDataConfig
 * @time 2018/6/6 10:39
 */
public class DateDataConfig extends AbstractDataItemConfig {
    /**
     * 时间格式
     */
    private String dateFormat;

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
