package com.zhujun.spider.master.extract.html;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

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
                } else {
                    throw new RuntimeException("不支持的dataType: " + dataType);
                }
            }

            // 设置公共属性
            dataItemConfig.setDataType(dataType);
            dataItemConfig.setName((String)scriptObjectMirror.get("name"));
            dataItemConfig.setNameSelector((String)scriptObjectMirror.get("nameSelector"));
            dataItemConfig.setSelector((String)scriptObjectMirror.get("selector"));
        }

        return dataItemConfig;
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
