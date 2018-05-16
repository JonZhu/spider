package com.zhujun.spider.master.controller;

import com.zhujun.spider.master.data.db.ISpiderTaskService;
import com.zhujun.spider.master.data.db.Page;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.dsl.DslParser;
import com.zhujun.spider.master.dsl.XmlDslParserImpl;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author zhujun
 * @desc TaskController
 * @time 2018/5/16 14:12
 */

@RestController
@RequestMapping("/api/task")
public class TaskController {
    private final static Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private ISpiderTaskService taskService;

    @RequestMapping(method = RequestMethod.POST)
    public Result<Void> createTask(HttpServletRequest httpRequest) throws IOException {
        InputStream inputStream = httpRequest.getInputStream();
        Spider spider = null;
        Result<Void> result = new Result();
        try {
            // 解析DSL
            DslParser dslParser = new XmlDslParserImpl();
            spider = dslParser.parse(inputStream);
        } catch (Exception e) {
            log.error("解析dsl失败", e);
            result.setStatus(1);
            result.setMsg("解析dsl失败");
            return result;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        try {
            taskService.createSpiderTask(spider);
        } catch (Exception e) {
            log.error("创建任务失败", e);
            result.setStatus(1);
            result.setMsg(e.getMessage());
        }

        return result;
    }

    @RequestMapping(value = "/{taskId}", method = RequestMethod.DELETE)
    public Result<Void> deleteTask(@PathVariable("taskId") String taskId) throws IOException {
        Result result = new Result();
        try {
            taskService.deleteSpiderTask(taskId);
        } catch (Exception e) {
            log.error("delete [{}] task fail", taskId, e);
            result.setStatus(1);
            result.setMsg("删除任务出错");
        }

        return result;
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public Result<Void> queryTask(Integer pageNo, Integer pageSize) throws IOException {
        Result result = new Result();
        try {
            Page<SpiderTaskPo> page = taskService.findSpiderTaskList(pageNo, pageSize);
            result.setData(page);
        } catch (Exception e) {
            result.setStatus(1);
            result.setMsg(e.getMessage());
            log.error("获取任务列表失败", e);
        }

        return result;
    }

    /**
     * 状态控制
     * @param taskId
     * @param toStatus
     * @return
     */
    @RequestMapping(value = "/status/{taskId}/{status}", method = RequestMethod.PUT)
    public Result statusControl(@PathVariable("taskId") String taskId, @PathVariable("status") Integer toStatus) {
        Result result = new Result();
        try {
            if (toStatus == SpiderTaskPo.Status.PAUSED) {
                // 暂停
                taskService.pauseTask(taskId);
            } else if (toStatus == SpiderTaskPo.Status.RUN) {
                // 恢复
                taskService.resumeTask(taskId);
            } else {
                // status值不正确
                result.setStatus(1);
                result.setMsg("toStatus值不正确");
            }
        } catch (Exception e) {
            result.setStatus(1);
            result.setMsg(e.getMessage());
        }

        return result;
    }

}
