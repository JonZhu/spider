package com.zhujun.spider.master;

import com.zhujun.spider.master.mina.MinaServer;
import com.zhujun.spider.master.schedule.IScheduleService;
import com.zhujun.spider.master.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Field;

/**
 * 启动 master
 * @author zhujun
 * @date 2016年6月3日
 *
 */
@SpringBootApplication
public class Startup {

	private final static Logger LOG = LoggerFactory.getLogger(Startup.class);
	
	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(Startup.class, args);
		setContext(context);
		// start ui server
		LOG.debug("ui server is running on port:{}", context.getEnvironment().getProperty("server.port"));

		// 启动任务调度
		context.getBean(IScheduleService.class).startupAllSchedule();
		
		// start mina server
		MinaServer minaServer = context.getBean(MinaServer.class);
		minaServer.start();
		LOG.debug("mina server is running on port:{}", minaServer.getPort());
		
	}

	private static void setContext(ConfigurableApplicationContext context) {
		try {
			Field field = SpringUtil.class.getDeclaredField("CONTEXT");
			field.setAccessible(true);
			field.set(SpringUtil.class, context);
		} catch (Exception e) {
			throw new RuntimeException("设置Spring context出错", e);
		}
	}

}
