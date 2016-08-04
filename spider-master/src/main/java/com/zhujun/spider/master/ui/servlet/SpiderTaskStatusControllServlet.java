package com.zhujun.spider.master.ui.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.zhujun.spider.master.data.db.ISpiderTaskService;
import com.zhujun.spider.master.data.db.SpiderTaskServiceImpl;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo.Status;
import com.zhujun.spider.master.di.DIContext;
import com.zhujun.spider.master.ui.JsonUtils;
import com.zhujun.spider.master.ui.Result;

/**
 * 任务状态控制
 * 
 * @author zhujun
 * @date 2016年8月4日
 *
 */
public class SpiderTaskStatusControllServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8854715681650348590L;

	private ISpiderTaskService spiderTaskService = DIContext.getInstance(ISpiderTaskService.class);
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String toStatusStr = req.getParameter("toStatus");
		String taskId = req.getParameter("taskId");
		
		Result result = new Result();
		if (StringUtils.isBlank(taskId)) {
			result.setStatus(1);
			result.setMsg("taskId参数不能为空");
			JsonUtils.writeValue(resp.getOutputStream(), result);
			return;
		}
		
		if (!NumberUtils.isDigits(toStatusStr)) {
			result.setStatus(1);
			result.setMsg("toStatus参数不正确");
			JsonUtils.writeValue(resp.getOutputStream(), result);
			return;
		}
		
		int toStatus = Integer.valueOf(toStatusStr);
		try {
			if (toStatus == Status.PAUSED) {
				// 暂停
				spiderTaskService.pauseTask(taskId);
			} else if (toStatus == Status.RUN) {
				// 恢复
				spiderTaskService.resumeTask(taskId);
			} else {
				// status值不正确
				result.setStatus(1);
				result.setMsg("toStatus值不正确");
			}
		} catch (Exception e) {
			result.setStatus(1);
			result.setMsg(e.getMessage());
		}
		
		JsonUtils.writeValue(resp.getOutputStream(), result);
		
	}
}
