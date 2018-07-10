package com.zhujun.spider.net.mina;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Spider网络传输数据消息
 * 
 * @author zhujun
 * @date 2016年6月17日
 *
 */
public class SpiderNetMessage {
	
	public final static String HEADER_BODY_LENGTH = "Content-Length";
	public final static String HEADER_CONTENTTYPE = "Content-Type";

	public final static String HEADER_MSGTYPE = "Spider-MsgType"; // 消息类型，用于区分业务
	public final static String HEADER_MSGID = "Spider-MsgId"; // 消息id，用于唯一标识消息
	public final static String HEADER_RESPONSEFOR = "Spider-ResponseFor"; // 响应的消息id，用于rpc
	public final static String HEADER_FETCHURL = "Spider-FetchUrl";
	public final static String HEADER_FETCHRESULT = "Spider-FetchResult";
	public final static String HEADER_FETCHTIME = "Spider-FetchTime";
	public final static String HEADER_STATUSCODE = "Spider-StatusCode"; // http response status code

	public final static String HEADER_TASKID = "Spider-TaskId";
	public final static String HEADER_ACTIONID = "Spider-ActionId";
	public final static String HEADER_URLID = "Spider-UrlId"; // fetchUrlId

	private Map<String, String> headers;
	
	private byte[] body;

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
	
	
	public void setHeader(String name, String value) {
		if (headers == null) {
			headers = new HashMap<>();
		}
		headers.put(name, value);
	}
	
	public String getHeader(String name) {
		return headers == null ? null : headers.get(name);
	}

	public void setFetchResult(boolean success) {
		if (success) {
			setHeader(HEADER_FETCHRESULT, "Success");
		} else {
			if (headers != null) {
				headers.remove(HEADER_FETCHRESULT);
			}
		}
	}

	public boolean getFetchResult() {
		return "Success".equals(getHeader(HEADER_FETCHRESULT));
	}

	public void setUrlId(String fetchUrlId) {
		setHeader(HEADER_URLID, fetchUrlId);
	}

	public String getUrlId() {
		return getHeader(HEADER_URLID);
	}

	public Date getFetchTime() {
		String timeStr = getHeader(HEADER_FETCHTIME);
		return timeStr == null ? null : new Date(Long.parseLong(timeStr));
	}

	public void setFetchTime(Date fetchTime) {
		setHeader(HEADER_FETCHTIME, String.valueOf(fetchTime.getTime()));
	}

	public void setStatusCode(int statusCode) {
		setHeader(HEADER_STATUSCODE, String.valueOf(statusCode));
	}

	public Integer getStatusCode() {
		String codeStr = getHeader(HEADER_STATUSCODE);
		return codeStr == null ? null : Integer.parseInt(codeStr);
	}


	public String getMsgType() {
		return getHeader(HEADER_MSGTYPE);
	}

	public void setMsgType(String msgType) {
		setHeader(HEADER_MSGTYPE, msgType);
	}

	public String getMsgId() {
		return getHeader(HEADER_MSGID);
	}

	public void setMsgId(String msgId) {
		setHeader(HEADER_MSGID, msgId);
	}

	public String getResponseFor() {
		return getHeader(HEADER_RESPONSEFOR);
	}

	public void setResponseFor(String responseFor) {
		setHeader(HEADER_RESPONSEFOR, responseFor);
	}

	public String getFetchUrl() {
		return getHeader(HEADER_FETCHURL);
	}

	public void setFetchUrl(String fetchUrl) {
		setHeader(HEADER_FETCHURL, fetchUrl);
	}

	public String getTaskId() {
		return getHeader(HEADER_TASKID);
	}

	public void setTaskId(String taskId) {
		setHeader(HEADER_TASKID, taskId);
	}

	public String getActionId() {
		return getHeader(HEADER_ACTIONID);
	}

	public void setActionId(String actionId) {
		setHeader(HEADER_ACTIONID, actionId);
	}

	public void setContentType(String contentType) {
		setHeader(HEADER_CONTENTTYPE, contentType);
	}

	public String getContentType() {
		return getHeader(HEADER_CONTENTTYPE);
	}



}
