package com.zhujun.spider.master.ui.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.data.db.ISpiderTaskService;
import com.zhujun.spider.master.data.db.Page;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.di.DIContext;
import com.zhujun.spider.master.ui.JsonUtils;
import com.zhujun.spider.master.ui.Result;

public class SpiderTaskListServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8854715681650348590L;

	private final static Logger LOG = LoggerFactory.getLogger(SpiderTaskListServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String pageNoStr = req.getParameter("pageNo");
		String pageSizeStr = req.getParameter("pageSize");
		
		int pageNo = StringUtils.isNumeric(pageNoStr) ? Integer.valueOf(pageNoStr) : 1;
		int pageSize = StringUtils.isNumeric(pageSizeStr) ? Integer.valueOf(pageSizeStr) : 20;
		
		Result result = new Result();
		try {
			Page<SpiderTaskPo> page = DIContext.getInstance(ISpiderTaskService.class).findSpiderTaskList(pageNo, pageSize);
			result.setData(page);
		} catch (Exception e) {
			result.setStatus(1);
			result.setMsg(e.getMessage());
			LOG.error("获取任务列表失败", e);
		}
		
		JsonUtils.writeValue(resp.getOutputStream(), result);
	}
}
