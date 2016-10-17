package com.zhujun.spider.net.mina;

public interface Consts {
	
	byte[] CRNL_BYTES = new byte[]{'\r', '\n'};
	
	String CRNL = new String(CRNL_BYTES);
	
	/**
	 * SpiderNetMessage 数据包开始标识
	 */
	byte[] MSG_START_BYTES = new byte[]{'S', 'P', 'I', 'D', 'E', 'R', '\r', '\n'};
	
}
