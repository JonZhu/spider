package com.zhujun.spider.master.domain;

/**
 * 用于克隆站点
 * 
 * @author zhujun
 * @date 2016年7月18日
 *
 */
public interface Clone extends DslAction {

	boolean isAllowCss();
	
	boolean isAllowJs();
	
	boolean isAllowImage();
	
	/**
	 * 种子
	 */
	String[] getSeeds();
	
	/**
	 * 限制范围, 仅作用于html
	 */
	String [] getHosts();
	
}
