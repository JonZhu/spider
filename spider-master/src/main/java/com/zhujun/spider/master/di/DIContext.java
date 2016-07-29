package com.zhujun.spider.master.di;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.zhujun.spider.master.data.db.FetchUrlServiceImpl;
import com.zhujun.spider.master.data.db.IFetchUrlService;
import com.zhujun.spider.master.data.db.ISpiderTaskService;
import com.zhujun.spider.master.data.db.SpiderTaskServiceImpl;
import com.zhujun.spider.master.schedule.IScheduleService;
import com.zhujun.spider.master.schedule.ScheduleServiceImpl;

/**
 * Dependence Inject
 * 
 * @author zhujun
 * @date 2016年6月24日
 *
 */
public class DIContext {

	private static Injector injector = Guice.createInjector(Stage.PRODUCTION, new Module() {
		@Override
		public void configure(Binder binder) {
			binder.bind(ISpiderTaskService.class).to(SpiderTaskServiceImpl.class);
			binder.bind(IFetchUrlService.class).to(FetchUrlServiceImpl.class);
			binder.bind(IScheduleService.class).to(ScheduleServiceImpl.class);
		}
	});
	
	public static <T> T getInstance(Class<T> type) {
		return injector.getInstance(type);
	}
	
	
	public static <T> void bind(final Class<T> type, final T instance) {
		injector = injector.createChildInjector((new Module() {
			@Override
			public void configure(Binder binder) {
				binder.bind(type).toInstance(instance);
			}
		}));
	}
	
	private DIContext() {}
	
}
