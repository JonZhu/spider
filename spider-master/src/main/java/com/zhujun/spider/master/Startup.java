package com.zhujun.spider.master;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.dsl.DslParser;
import com.zhujun.spider.master.dsl.XmlDslParserImpl;
import com.zhujun.spider.master.schedule.SpiderSchedule;
import com.zhujun.spider.master.schedule.SpiderScheduleImpl;

/**
 * 启动 master
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public class Startup {

	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			System.err.println("useage: Startup spiderdsl.xml");
			System.exit(-1);
		}
		
		String dslfile = args[0];
		InputStream dslInput = null;
		try {
			dslInput = new FileInputStream(dslfile);
		} catch (FileNotFoundException e) {
			System.err.println("dsl file ["+ dslfile +"] 不存在");
			System.exit(-1);
		}
		
		Spider spider = null;
		try {
			// 解析DSL
			DslParser dslParser = new XmlDslParserImpl();
			spider = dslParser.parse(dslInput);
		} catch (Exception e) {
			System.err.println("解析dsl失败");
			e.printStackTrace();
			System.exit(-1);
		} finally {
			IOUtils.closeQuietly(dslInput);
		}
		
		// 执行
		SpiderSchedule schedule = new SpiderScheduleImpl(spider);
		schedule.start();
		
	}

}
