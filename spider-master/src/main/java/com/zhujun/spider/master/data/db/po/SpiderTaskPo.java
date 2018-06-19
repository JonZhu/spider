package com.zhujun.spider.master.data.db.po;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Time;

@Document(collection = "spider_task")
public class SpiderTaskPo {

	public static interface Status {
		int NEW = 0;
		
		int RUN = 1;
		
		int PAUSED = 4;
		
		int COMPLETE = 6;

		int DELETED = 7;
	}

	@Id
	private String id;
	
	private String name;
	
	private String author;
	
	private String datadir;
	
	private Time createTime;
	
	private int status = Status.NEW;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDatadir() {
		return datadir;
	}

	public void setDatadir(String datadir) {
		this.datadir = datadir;
	}

	public Time getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Time createTime) {
		this.createTime = createTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}
