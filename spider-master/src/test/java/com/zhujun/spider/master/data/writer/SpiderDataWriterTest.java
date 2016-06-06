package com.zhujun.spider.master.data.writer;

import static org.junit.Assert.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class SpiderDataWriterTest {

	@Test
	public void testFileExist() {
		
		File file = new File("E:/tmp/11111111111111111.txt");
		System.out.println(file.exists());
		System.out.println(file.getParentFile().exists());
		
	}
	
	@Test
	public void testDateFormat() {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		System.out.println(format.format(new Date()));
		
	}

}
