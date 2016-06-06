package com.zhujun.spider.master.data.writer;

import java.util.Date;

/**
 * Spider数据写
 * 
 * <p>用于spider数据存储</p>
 * 
 * @author zhujun
 * @date 2016年6月6日
 *
 */
public interface SpiderDataWriter {

	/**
	 * 写
	 * 
	 * @author zhujun
	 * @date 2016年6月6日
	 *
	 * @param originUrl 来源url
	 * @param fetchTime 抓取时间
	 * @param contentData 内容数据
	 */
	void write(String originUrl, Date fetchTime, byte[] contentData);
	
}
