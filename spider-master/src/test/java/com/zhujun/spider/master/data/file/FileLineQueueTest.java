package com.zhujun.spider.master.data.file;

import java.util.Queue;

import org.junit.Test;

public class FileLineQueueTest {

	@Test
	public void test() {
		
		try {
			
			Queue<String> queue = new FileLineQueue("E:/tmp/spider/meituan/fetchqueue");
			
			queue.add("11111111111");
			queue.add("11111111111");
			queue.add("22222222222");
			
			System.out.println(queue.poll());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
