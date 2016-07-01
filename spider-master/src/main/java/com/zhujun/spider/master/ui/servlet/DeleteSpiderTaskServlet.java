package com.zhujun.spider.master.ui.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.data.db.ISpiderTaskService;
import com.zhujun.spider.master.di.DIContext;
import com.zhujun.spider.master.ui.JsonUtils;
import com.zhujun.spider.master.ui.Result;

public class DeleteSpiderTaskServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8854715681650348590L;

	private final static Logger LOG = LoggerFactory.getLogger(DeleteSpiderTaskServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String taskId = req.getParameter("id");
		Result result = new Result();
		try {
			DIContext.getInstance(ISpiderTaskService.class).deleteSpiderTask(taskId);
		} catch (Exception e) {
			LOG.error("删除[{}]任务出错", taskId, e);
			result.setStatus(1);
			result.setMsg("删除任务出错");
		}
		
		JsonUtils.writeValue(resp.getOutputStream(), result);
		
	}
}
