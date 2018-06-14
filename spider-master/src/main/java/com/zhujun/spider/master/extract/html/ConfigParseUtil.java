package com.zhujun.spider.master.extract.html;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 配置解析工具
 *
 * @author zhujun
 * @desc ConfigParseUtil
 * @time 2018/6/6 11:34
 */
public class ConfigParseUtil {

    public static DataItemConfig parseJsConfig(String config) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        Object result = null;
        try {
            result = engine.eval(config);
        } catch (ScriptException e) {
            throw new RuntimeException("配置语法错误", e);
        }

        return convertConfigData(result);
    }

    private static DataItemConfig convertConfigData(Object jsObject) {
        AbstractDataItemConfig dataItemConfig = null;
        //ScriptObjectMirror
        if (jsObject instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) jsObject;
            ScriptObjectMirror ref = (ScriptObjectMirror)scriptObjectMirror.get("ref");
            String dataType = (String)scriptObjectMirror.get("dataType");
            if (ref != null) {
                // 引用数据类型
                ReferenceDataConfig referenceDataConfig = new ReferenceDataConfig();
                referenceDataConfig.setRef(convertConfigData(ref));
                dataItemConfig = referenceDataConfig;
            } else {
                // 非引用类型,按dataType转换
                if (DataItemConfig.DATA_TYPE_NUMBER.equals(dataType) || DataItemConfig.DATA_TYPE_STRING.equals(dataType)) {
                    dataItemConfig = new BasicDataConfig();
                } else if (DataItemConfig.DATA_TYPE_OBJECT.equals(dataType)) {
                    dataItemConfig = convertObjectConfig(scriptObjectMirror);
                } else if (DataItemConfig.DATA_TYPE_ARRAY.equals(dataType)) {
                    dataItemConfig = convertArrayConfig(scriptObjectMirror);
                } else {
                    throw new RuntimeException("不支持的dataType: " + dataType);
                }
            }

            // 设置公共属性
            dataItemConfig.setDataType(dataType);
            dataItemConfig.setName((String)scriptObjectMirror.get("name"));
            dataItemConfig.setNameSelector((String)scriptObjectMirror.get("nameSelector"));
            dataItemConfig.setSelector((String)scriptObjectMirror.get("selector"));
            dataItemConfig.setCondition(convertCondition(scriptObjectMirror));
        }

        return dataItemConfig;
    }

    /**
     * 解析condition
     * @param scriptObjectMirror
     * @return
     */
    private static Condition convertCondition(ScriptObjectMirror scriptObjectMirror) {
        Object conditionConfig = scriptObjectMirror.get("condition");
        if (conditionConfig == null) {
            return null;
        }

        if (!(conditionConfig instanceof ScriptObjectMirror)) {
            throw new RuntimeException("condition应该为对象");
        }

        ScriptObjectMirror conditionObj = (ScriptObjectMirror)conditionConfig;
        String elementSelector = (String)conditionObj.get("elementSelector");
        String urlPattern = (String)conditionObj.get("urlPattern");
        if (elementSelector == null && urlPattern == null) {
            throw new RuntimeException("condition的属性elementSelector、urlPattern至少需要一个");
        }

        Condition condition = new Condition();
        condition.setElementSelector(elementSelector);
        if (urlPattern != null) {
            condition.setUrlPatter(Pattern.compile(urlPattern));
        }
        return condition;
    }

    private static ArrayDataConfig convertArrayConfig(ScriptObjectMirror scriptObjectMirror) {
        DataItemConfig itemConfig = convertConfigData(scriptObjectMirror.get("itemData"));

        if (itemConfig == null) {
            throw new RuntimeException("数组itemData配置不正确");
        }

        ArrayDataConfig config = new ArrayDataConfig();
        config.setItemData(itemConfig);

        // startSkip
        Object tempValue = scriptObjectMirror.get("startSkip");
        if (tempValue != null) {
            if (tempValue instanceof Number) {
                config.setStartSkip(((Number) tempValue).intValue());
            } else {
                throw new RuntimeException("数组startSkip必需为数字");
            }
        }

        // endSkip
        tempValue = scriptObjectMirror.get("endSkip");
        if (tempValue != null) {
            if (tempValue instanceof Number) {
                config.setEndSkip(((Number) tempValue).intValue());
            } else {
                throw new RuntimeException("数组endSkip必需为数字");
            }
        }

        return config;
    }

    private static ObjectDataConfig convertObjectConfig(ScriptObjectMirror scriptObjectMirror) {
        ScriptObjectMirror propsObjMirror = (ScriptObjectMirror)scriptObjectMirror.get("properties");
        if (propsObjMirror == null || !propsObjMirror.isArray() || propsObjMirror.isEmpty()) {
            throw new RuntimeException("properties应为非空的数组");
        }

        int index = 0;
        Object propObj = null;
        List<DataItemConfig> propDataItemConfigList = new ArrayList<>();
        while (true) {
            propObj = propsObjMirror.get(index + "");
            index++;
            if (propObj == null) {
                break;
            }

            if (propObj instanceof ScriptObjectMirror) {
                // 递归转换属性配置
                DataItemConfig propConfig = convertConfigData(propObj);
                if (!(propConfig.getName() != null || propConfig.getNameSelector() != null)) {
                    throw new RuntimeException("对象属性名不能为空");
                }
                propDataItemConfigList.add(propConfig);
            }

        }

        if (propDataItemConfigList.size() == 0) {
            throw new RuntimeException("properties内容配置不正确");
        }

        ObjectDataConfig config = new ObjectDataConfig();
        config.setProperties(propDataItemConfigList.toArray(new DataItemConfig[]{}));
        return config;
    }

}
