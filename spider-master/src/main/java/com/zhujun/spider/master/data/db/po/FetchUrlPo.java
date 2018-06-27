package com.zhujun.spider.master.data.db.po;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 抓取url
 * 
 * @author zhujun
 * @date 2016年7月4日
 *
 */
@Document(collection = "fetchurl")
public class FetchUrlPo {

	public final static int STATUS_INIT = 0;
	public final static int STATUS_PUSHED = 2;
	public final static int STATUS_SUCCESS = 3;
	public final static int STATUS_ERROR = 4;
	
	@Id
	private String id;
	
	private String url;
	
	/**
	 * 状态: 0初始态, 2 已下发, 3 抓取成功, 4 抓取失败
	 */
	private int status = STATUS_INIT;
	
	private Date insertTime;
	
	private Date modifyTime;
	
	/**
	 * spider中哪个action生成的url
	 */
	private String actionId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public Date getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	
}
