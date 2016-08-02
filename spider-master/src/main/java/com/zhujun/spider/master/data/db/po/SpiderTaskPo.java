package com.zhujun.spider.master.data.db.po;

import java.sql.Time;

public class SpiderTaskPo {

	public static enum Status {
		NEW(0), RUN(1), PAUSED(4), COMPLETE(6);
		
		private int value;
		
		private Status(int value) {
			this.value = value;
		}
		
		public static Status valueOf(int value) {
			return value == 0 ? NEW : (value == 1 ? RUN : (value == 4 ? PAUSED : (value == 6 ? COMPLETE : null)));
		}
	}
	
	private String id;
	
	private String name;
	
	private String author;
	
	private String datadir;
	
	private Time createTime;
	
	private Status status;

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
	
	
}
