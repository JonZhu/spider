package com.zhujun.spider.master.extract.html;

/**
 * 对象数据配置
 *
 * @author zhujun
 * @desc ObjectDataConfig
 * @time 2018/6/6 10:34
 */
public class ObjectDataConfig extends AbstractDataItemConfig {
    /**
     * 对象属性
     */
    private DataItemConfig[] properties;

    public DataItemConfig[] getProperties() {
        return properties;
    }

    public void setProperties(DataItemConfig[] properties) {
        this.properties = properties;
    }
}
