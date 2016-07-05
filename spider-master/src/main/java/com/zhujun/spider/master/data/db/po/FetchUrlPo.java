package com.zhujun.spider.master.data.db.po;

import java.sql.Time;

/**
 * 抓取url
 * 
 * @author zhujun
 * @date 2016年7月4日
 *
 */
public class FetchUrlPo {

	private Integer id;
	
	private String url;
	
	/**
	 * 状态: 0初始态, 2 已下发, 3 抓取成功, 4 抓取失败
	 */
	private int status;
	
	private Time insertTime;
	
	private Time Modifytime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Time getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Time insertTime) {
		this.insertTime = insertTime;
	}

	public Time getModifytime() {
		return Modifytime;
	}

	public void setModifytime(Time modifytime) {
		Modifytime = modifytime;
	}
	
}
