package com.zhujun.spider.master.di;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.zhujun.spider.master.data.db.FetchUrlService;
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
			binder.bind(IFetchUrlService.class).to(FetchUrlService.class);
			binder.bind(IScheduleService.class).to(ScheduleServiceImpl.class);
//				binder.bind(Service1.class);
//				binder.bind(Service2.class);
		}
	});
	
	public static <T> T getInstance(Class<T> type) {
		return injector.getInstance(type);
	}
	
	private DIContext() {}
	
}
