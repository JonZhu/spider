package com.zhujun.spider.master.data.db.po;

import org.springframework.data.annotation.Id;

import java.sql.Time;

/**
 * 抓取url
 * 
 * @author zhujun
 * @date 2016年7月4日
 *
 */
public class FetchUrlPo {

	public final static int STATUS_INIT = 0;
	public final static int STATUS_PUSHED = 2;
	public final static int STATUS_SUCCESS = 3;
	public final static int STATUS_ERROR = 4;
	
	@Id
	private Integer id;
	
	private String url;
	
	/**
	 * 状态: 0初始态, 2 已下发, 3 抓取成功, 4 抓取失败
	 */
	private int status = STATUS_INIT;
	
	private Time insertTime;
	
	private Time Modifytime;
	
	/**
	 * spider中哪个action生成的url
	 */
	private String actionId;

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

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	
}
