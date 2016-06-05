package com.zhujun.spider.master.schedule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class DataTransitionExecutorTest {

	@Test
	public void testReg() {
		Matcher matcher = Pattern.compile("http://([\\d\\w]+)").matcher("http://als.meituan.com");
//		System.out.println(matcher.find() && matcher.groupCount() > 1);
		
		System.out.println(matcher.find());
		System.out.println(matcher.groupCount());
		System.out.println(matcher.group(0));
		System.out.println(matcher.group(1));
	}

}
