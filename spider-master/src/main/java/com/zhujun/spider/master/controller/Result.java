package com.zhujun.spider.master.controller;

/**
 * 接口响应数据
 * 
 * @author zhujun
 * @date 2016年6月30日
 *
 */
public class Result<T> {

	/**
	 * 0 成功, 非0 失败
	 */
	private int status = 0;
	
	private String msg;
	
	private T data;

	public Result(){}

	public Result(T data) {
		this.data = data;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
}
