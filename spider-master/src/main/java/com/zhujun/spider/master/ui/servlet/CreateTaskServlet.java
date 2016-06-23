package com.zhujun.spider.master.ui.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.dsl.DslParser;
import com.zhujun.spider.master.dsl.XmlDslParserImpl;

/**
 * 创建任务
 * 
 * @author zhujun
 * @date 2016年6月23日
 *
 */
public class CreateTaskServlet extends HttpServlet {

	private final static Logger LOG = LoggerFactory.getLogger(CreateTaskServlet.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8854715681650348590L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		resp.getWriter().println("servlet test");
		
		InputStream inputStream = req.getInputStream();
		Spider spider = null;
		try {
			// 解析DSL
			DslParser dslParser = new XmlDslParserImpl();
			spider = dslParser.parse(inputStream);
		} catch (Exception e) {
			LOG.error("解析dsl失败", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		
		
		
	}
}
