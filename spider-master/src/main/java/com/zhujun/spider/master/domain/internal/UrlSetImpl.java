package com.zhujun.spider.master.domain.internal;

import java.util.Map;

import com.zhujun.spider.master.domain.UrlSet;

public class UrlSetImpl extends DslParentActionImpl implements UrlSet {

	private String urltemplate;
	
	private Map<Integer, String> typeMap;
	
	private Map<Integer, String> valueMap;
	
	
	public void setUrltemplate(String urltemplate) {
		this.urltemplate = urltemplate;
	}

	@Override
	public String getUrltemplate() {
		return urltemplate;
	}

	@Override
	public String getTempType(int index) {
		return typeMap == null ? null : typeMap.get(index);
	}

	@Override
	public String getTempValue(int index) {
		return valueMap == null ? null : valueMap.get(index);
	}
	
	
	public void setTypeMap(Map<Integer, String> typeMap) {
		this.typeMap = typeMap;
	}

	public void setValueMap(Map<Integer, String> valueMap) {
		this.valueMap = valueMap;
	}

}
