package com.zhujun.spider.master.extract.html;

/**
 * 数据项配置
 *
 * @author zhujun
 * @desc DataItemConfig
 * @time 2018/6/6 10:27
 */
public interface DataItemConfig extends Cloneable {
    String DATA_TYPE_STRING = "string";
    String DATA_TYPE_NUMBER = "number";
    String DATA_TYPE_DATE = "date";
    String DATA_TYPE_OBJECT = "object";
    String DATA_TYPE_ARRAY = "array";

    /**
     * 条件， 在条件满足时才抽取该数据
     * @return
     */
    Condition getCondition();

    /**
     * 名称, 做为object的属性时必需
     * @return
     */
    String getName();

    /**
     * 动态名称, 做为object的动态属性时必需
     * @return
     */
    String getNameSelector();

    /**
     * 数据类型 object|array|number|string|date
     * @return
     */
    String getDataType();

    /**
     * 值选择器, 可选
     * @return
     */
    String getSelector();

}
