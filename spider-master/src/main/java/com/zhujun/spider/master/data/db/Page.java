package com.zhujun.spider.master.data.db;

import java.util.List;

/**
 * 数据分页
 * 
 * @author zhujun
 * @date 2016年6月29日
 *
 */
public class Page<T> {

	/**
	 * 计算页总数
	 * 
	 * @param dataTotal
	 * @param pageSize
	 * @return
	 */
	public static int calculatePageTotal(int dataTotal, int pageSize) {
		return dataTotal % pageSize > 0 ? (dataTotal / pageSize + 1) : dataTotal / pageSize;
	}
	
	/**
	 * 页大小
	 */
	private int pageSize;
	
	/**
	 * 页号, 从1开始
	 */
	private int pageNo;
	
	/**
	 * 页总数
	 */
	private int pageTotal;
	
	/**
	 * 数据总数
	 */
	private int dataTotal;
	
	/**
	 * 当前页数据
	 */
	private List<T> pageData;

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageTotal() {
		return pageTotal;
	}

	public void setPageTotal(int pageTotal) {
		this.pageTotal = pageTotal;
	}

	public int getDataTotal() {
		return dataTotal;
	}

	public void setDataTotal(int dataTotal) {
		this.dataTotal = dataTotal;
	}

	public List<T> getPageData() {
		return pageData;
	}

	public void setPageData(List<T> pageData) {
		this.pageData = pageData;
	}
	
}
