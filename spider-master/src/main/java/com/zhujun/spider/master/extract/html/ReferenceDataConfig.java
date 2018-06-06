package com.zhujun.spider.master.extract.html;

/**
 * 引用数据类型
 *
 * <p>引用数据类型的dataType为null，类型于ref目标决定, 一般用于对象属性</p>
 *
 * @author zhujun
 * @desc ReferenceDataConfig
 * @time 2018/6/6 14:22
 */
public class ReferenceDataConfig extends AbstractDataItemConfig {

    private DataItemConfig ref;

    public DataItemConfig getRef() {
        return ref;
    }

    public void setRef(DataItemConfig ref) {
        this.ref = ref;
    }
}
