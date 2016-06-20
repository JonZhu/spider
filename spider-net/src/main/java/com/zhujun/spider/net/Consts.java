package com.zhujun.spider.net;

public interface Consts {

	String CRNL = "\\r\\n";
	
	byte[] CRNL_BYTES = CRNL.getBytes();
	
	/**
	 * SpiderNetMessage 数据包开始标识
	 */
	byte[] MSG_START_BYTES = ("SPIDER" + CRNL).getBytes();
	
}
