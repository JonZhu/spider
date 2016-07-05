package com.zhujun.spider.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.di.DIContext;
import com.zhujun.spider.master.mina.MinaServer;
import com.zhujun.spider.master.schedule.IScheduleService;
import com.zhujun.spider.master.ui.UIServer;

/**
 * 启动 master
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public class Startup {

	private final static Logger LOG = LoggerFactory.getLogger(Startup.class);
	
	public static void main(String[] args) {
		
		// 启动任务调度
		DIContext.getInstance(IScheduleService.class).startupAllSchedule();
		
		// start mina server
		int port  = args.length > 0 ? Integer.valueOf(args[0]) : 8619;
		LOG.debug("开始启动MinaServer, port:{}", port);
		MinaServer minaServer = new MinaServer(port);
		minaServer.start();
		LOG.debug("启动完成MinaServer, port:{}", port);
		
		// start ui server
		LOG.debug("开始启动UIServer, port:8618");
		UIServer uiServer = new UIServer(8618);
		uiServer.start();
		LOG.debug("启动完成UIServer, port:8618");
		
	}

}
