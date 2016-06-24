package com.zhujun.spider.master.guice;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GuiceTest {

	@Test
	public void testCycle() {
		
		Injector injector = Guice.createInjector(new Module() {
			
			@Override
			public void configure(Binder binder) {
				binder.bind(Service1.class);
				binder.bind(Service2.class);
			}
		});
		
		Service1 service1 = injector.getInstance(Service1.class);
		service1.print();
		
		Service1 service11 = injector.getInstance(Service1.class);
		service11.print();
		
	}
	
	
	
	@Singleton
	private static class Service1 {
		
		@Inject
		private Service2 service2;
		
		
		private void print() {
			System.out.println(service2);
		}
	}
	
	@Singleton
	private static class Service2 {
		
		@Inject
		private Service1 service1;
		
		private void print() {
			System.out.println(service1);
		}
		
	}
	
	
}
