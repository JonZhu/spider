package com.zhujun.spider.master;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.dsl.DslParser;
import com.zhujun.spider.master.dsl.XmlDslParserImpl;
import com.zhujun.spider.master.mina.MinaServer;
import com.zhujun.spider.master.schedule.SpiderSchedule;
import com.zhujun.spider.master.schedule.SpiderScheduleImpl;
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
//		if (args == null || args.length < 1) {
//			System.err.println("useage: Startup spiderdsl.xml");
//			System.exit(-1);
//		}
//		
//		String dslfile = args[0];
//		InputStream dslInput = null;
//		try {
//			dslInput = new FileInputStream(dslfile);
//		} catch (FileNotFoundException e) {
//			System.err.println("dsl file ["+ dslfile +"] 不存在");
//			System.exit(-1);
//		}
//		
//		Spider spider = null;
//		try {
//			// 解析DSL
//			DslParser dslParser = new XmlDslParserImpl();
//			spider = dslParser.parse(dslInput);
//		} catch (Exception e) {
//			System.err.println("解析dsl失败");
//			e.printStackTrace();
//			System.exit(-1);
//		} finally {
//			IOUtils.closeQuietly(dslInput);
//		}
//		
//		// 执行
//		SpiderSchedule schedule = new SpiderScheduleImpl(spider);
//		schedule.start();
		
		
		// start mina server
		int port  = args.length > 0 ? Integer.valueOf(args[0]) : 8619;
		LOG.debug("开始启动MinaServer, port:{}", port);
		MinaServer minaServer = new MinaServer(port);
		minaServer.start();
		LOG.debug("启动完成MinaServer, port:{}", port);
		
		LOG.debug("开始启动UIServer, port:8618");
		UIServer uiServer = new UIServer(8618);
		uiServer.start();
		LOG.debug("启动完成UIServer, port:8618");
		
	}

}
