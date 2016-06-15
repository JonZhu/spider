package com.zhujun.spider.master.data.file;

import org.junit.Test;

public class FileLineQueueTest {

	@Test
	public void test() {
		
		try {
			
			FileLineQueue queue = new FileLineQueue("E:/tmp/spider/meituan/fetchqueue");
			
			queue.add("11111111111");
			queue.add("11111111111");
			queue.add("22222222222");
			
			System.out.println(queue.poll());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Test
	public void testPoll() {
		try {
			FileLineQueue queue = new FileLineQueue("E:/tmp/spider/meituan/fetchqueue");
			
			System.out.println(queue.poll());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void testAddLarge() {
		
		try {
			
			FileLineQueue queue = new FileLineQueue("E:/tmp/spider/meituan/fetchqueue");
			
			long addStart = System.currentTimeMillis();
			for (int i = 0; i < 1000000; i++) {
				queue.add("http://www.meituan.com/page" + i);
				System.out.println(i);
			}
			
			System.out.println("add time: " + (System.currentTimeMillis() - addStart));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Test
	public void testPollLarge() {
		try {
			
			FileLineQueue queue = new FileLineQueue("E:/tmp/spider/meituan/fetchqueue");
			
			long addStart = System.currentTimeMillis();
			for (int i = 0; i < 1000000; i++) {
				queue.poll();
				System.out.println(i);
			}
			
			System.out.println("add time: " + (System.currentTimeMillis() - addStart));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
