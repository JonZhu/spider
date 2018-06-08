package com.zhujun.spider.master.extract.html;

/**
 * 数组 数据配置
 * @author zhujun
 * @desc ArrayDataConfig
 * @time 2018/6/6 10:38
 */
public class ArrayDataConfig extends AbstractDataItemConfig {

    /**
     * 数组 项
     */
    private DataItemConfig itemData;

    /**
     * 开始跳过的项目数量
     */
    private Integer startSkip;

    /**
     * 结束跳过的项目数量
     */
    private Integer endSkip;

    public Integer getStartSkip() {
        return startSkip;
    }

    public void setStartSkip(Integer startSkip) {
        this.startSkip = startSkip;
    }

    public Integer getEndSkip() {
        return endSkip;
    }

    public void setEndSkip(Integer endSkip) {
        this.endSkip = endSkip;
    }

    public DataItemConfig getItemData() {
        return itemData;
    }

    public void setItemData(DataItemConfig itemData) {
        this.itemData = itemData;
    }
}
