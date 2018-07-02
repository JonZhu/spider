package com.zhujun.spider.master.controller;

import com.zhujun.spider.master.data.db.IHtmlExtractService;
import com.zhujun.spider.master.data.db.po.HtmlExtractTaskPo;
import com.zhujun.spider.master.extract.html.ConfigParseUtil;
import com.zhujun.spider.master.extract.html.DataItemConfig;
import com.zhujun.spider.master.extract.html.HtmlExtractor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * html数据抽取
 *
 * @author zhujun
 * @date 2018-06-23
 */
@RestController
@RequestMapping("/api/html/extract")
public class HtmlExtractController {

    @Autowired
    private IHtmlExtractService htmlExtractService;

    /**
     * 抽取url中的数据
     * @param url
     * @param extractConfig 配置
     * @return
     */
    @RequestMapping(value = "/url", method = RequestMethod.POST)
    public Result extractUrl(String url, String extractConfig) {
        // 验证、解析配置
        DataItemConfig dataItemConfig = ConfigParseUtil.parseJsConfig(extractConfig);

        // 获取内容
        URL urlObj = null;
        try {
            urlObj = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("url不正确", e);
        }

        HttpURLConnection urlConnection = null;
        int httpStatus;
        try {
            // 连接
            urlConnection = (HttpURLConnection)urlObj.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);
            httpStatus = urlConnection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException("连接失败", e);
        }

        if (httpStatus < 200 || httpStatus > 299) {
            throw new RuntimeException("http status异常: " + httpStatus);
        }

        String contentType = urlConnection.getHeaderField("Content-Type");
        if (!contentType.startsWith("text/html")) {
            throw new RuntimeException("content type应该为text/html");
        }

        ByteArrayOutputStream byteArrayOutputStream = null;
        InputStream inputStream = null;
        try {
            inputStream = urlConnection.getInputStream();
            byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, byteArrayOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("读取数据失败", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        // 抽取数据
        HtmlExtractor htmlExtractor = new HtmlExtractor(dataItemConfig);
        Object extractResult = htmlExtractor.extract(url, contentType, byteArrayOutputStream.toByteArray());

        return new Result(extractResult);
    }

    @RequestMapping(value = "/task", method = RequestMethod.POST)
    public Result<HtmlExtractTaskPo> createExtractTask(String extractConfig, String taskName, String srcDataDir,
                                                       String mongoDbName, String mongoCollectionName) {
        HtmlExtractTaskPo task = new HtmlExtractTaskPo();
        task.setExtractConfig(extractConfig);
        task.setTaskName(taskName);
        task.setSrcDataDir(srcDataDir);
        task.setMongoDbName(mongoDbName);
        task.setMongoCollectionName(mongoCollectionName);
        return new Result(htmlExtractService.createExtractTask(task));
    }

    @RequestMapping(value = "/task/status/{taskId}/{newStatus}", method = RequestMethod.PUT)
    public Result changeTaskStatus(@PathVariable("taskId") String taskId, @PathVariable("newStatus") int newStatus) {
        htmlExtractService.chnageTaskStatus(taskId, newStatus);
        return new Result();
    }

    @RequestMapping(value = "/task/{taskId}", method = RequestMethod.DELETE)
    public Result deleteTask(@PathVariable("taskId") String taskId) {
        htmlExtractService.deleteTask(taskId);
        return new Result();
    }

    @RequestMapping(value = "/task", method = RequestMethod.GET)
    public Result findTask() {
        List<HtmlExtractTaskPo> taskList = htmlExtractService.findTask();
        return new Result(taskList);
    }

}
