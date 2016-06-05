package com.zhujun.spider.master.domain;

import java.util.List;

/**
 * Url集
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public interface UrlSet extends DslParentAction {

	
	String getUrltemplate();
	
	/**
	 * 模板索引号List
	 * @return
	 */
	List<Integer> getTempIndexList();
	
	/**
	 * 获取tempType类型
	 * 
	 * @param index 序号,从1开始
	 * @return "enum, int"
	 */
	String getTempType(int index);
	
	/**
	 * 获取tempValue值
	 *
	 * @param index 序号,从1开始
	 * @return
	 */
	String getTempValue(int index);

}
