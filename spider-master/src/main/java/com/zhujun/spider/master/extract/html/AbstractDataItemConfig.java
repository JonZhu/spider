package com.zhujun.spider.master.extract.html;

/**
 * @author zhujun
 * @desc AbstractDataItemConfig
 * @time 2018/6/6 10:30
 */
public abstract class AbstractDataItemConfig implements DataItemConfig {
    private Condition condition;

    private String name;

    private String nameSelector;

    private String dataType;

    private String selector;

    @Override
    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getNameSelector() {
        return nameSelector;
    }

    public void setNameSelector(String nameSelector) {
        this.nameSelector = nameSelector;
    }

    @Override
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
