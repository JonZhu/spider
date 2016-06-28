package com.zhujun.spider.master.ui.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zhujun.spider.master.data.db.ISpiderTaskService;
import com.zhujun.spider.master.di.DIContext;

public class DeleteSpiderTaskServlet extends HttpServlet {

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
		
		String taskId = req.getParameter("id");
		try {
			DIContext.getInstance(ISpiderTaskService.class).deleteSpiderTask(taskId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
