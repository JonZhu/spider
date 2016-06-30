package com.zhujun.spider.master.ui;

/**
 * 接口响应数据
 * 
 * @author zhujun
 * @date 2016年6月30日
 *
 */
public class Result {

	/**
	 * 0 成功, 非0 失败
	 */
	private int status = 0;
	
	private String msg;
	
	private Object data;

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

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
