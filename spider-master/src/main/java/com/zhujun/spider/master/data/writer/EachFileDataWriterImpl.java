package com.zhujun.spider.master.data.writer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FileUtils;

/**
 * 每个页面, 单独写文件
 * 
 * @author zhujun
 * @date 2016年7月19日
 *
 */
public class EachFileDataWriterImpl implements SpiderDataWriter {

	final private String dir;
	
	public EachFileDataWriterImpl(String dir) {
		this.dir = dir;
	}
	
	@Override
	public void write(String originUrl, Date fetchTime, byte[] contentData) throws IOException {

		URL urlObj = new URL(originUrl);
		File hostDir = new File(dir, urlObj.getHost());
		if (!hostDir.exists()) {
			hostDir.mkdirs();
		}
		
		File dataFile = new File(hostDir, urlObj.getFile());
		FileUtils.writeByteArrayToFile(dataFile, contentData);
		
	}

}
