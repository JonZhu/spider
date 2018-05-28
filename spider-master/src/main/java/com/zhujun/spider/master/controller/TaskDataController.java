package com.zhujun.spider.master.controller;

import com.zhujun.export.appendfile.AppendFileReader;
import com.zhujun.export.appendfile.MetaData;
import com.zhujun.spider.master.controller.vo.DataFileVo;
import com.zhujun.spider.master.data.db.ISpiderTaskService;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 任务数据接口
 *
 * @author zhujun
 * @desc TaskDataController
 * @time 2018/5/28 10:42
 */
@RestController
@RequestMapping("/api/data")
public class TaskDataController {

    @Autowired
    private ISpiderTaskService spiderTaskService;

    /**
     * 获取任务数据文件列表
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/datafilelist", method = RequestMethod.GET)
    public Result<List<DataFileVo>> getDataFileList(String taskId) throws Exception {
        File dataDir = getTaskDataDir(taskId);

        File[] dataFileArray = dataDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("data-");
            }
        });
        if (dataFileArray == null || dataFileArray.length == 0) {
            return null;
        }

        List<DataFileVo> dataFileList = new ArrayList<>(dataFileArray.length);
        for (File dataFile : dataFileArray) {
            DataFileVo dataFileVo = new DataFileVo();
            dataFileVo.setName(dataFile.getName());
            dataFileVo.setCreateTime(getFileCreateTime(dataFile));
            dataFileVo.setModifyTime(new Date(dataFile.lastModified()));
            dataFileVo.setTaskId(taskId);
            dataFileVo.setSize(dataFile.length());
            dataFileList.add(dataFileVo);
        }

        return new Result(dataFileList);
    }

    private File getTaskDataDir(String taskId) throws Exception {
        SpiderTaskPo spiderTask = spiderTaskService.getSpiderTask(taskId);
        if (spiderTask == null) {
            throw new RuntimeException("task不存在");
        }

        if (spiderTask.getDatadir() == null) {
            throw new RuntimeException("task目录数据不正确");
        }

        File dataDir = new File(spiderTask.getDatadir());
        if (!dataDir.isDirectory()) {
            throw new RuntimeException("task目录不存在");
        }
        return dataDir;
    }

    private Date getFileCreateTime(File file) {
        BasicFileAttributeView fileAttributeView = Files.getFileAttributeView(file.toPath(),
                BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        FileTime fileTime = null;
        try {
            fileTime = fileAttributeView.readAttributes().creationTime();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileTime == null ? null : new Date(fileTime.toMillis());
    }


    /**
     * 获取文件元数据
     * @return
     */
    @RequestMapping(value = "/metadata")
    public Result<List<MetaData>> getMetaData(String taskId, String dataFileName, Long offset, long count) throws Exception {
        File dataDir = getTaskDataDir(taskId);
        File appendFile = new File(dataDir, dataFileName);
        if (!appendFile.isFile()) {
            throw new RuntimeException("文件名错误");
        }

        AppendFileReader reader = new AppendFileReader(appendFile);
        if (offset != null) {
            reader.setOffset(offset);
        }

        List<MetaData> metaDataList = new ArrayList<>();
        MetaData metaData = null;
        for (int i = 0; i < count; i++) {
            metaData = reader.readMetaData();
            if (metaData == null) {
                // 读完数据文件
                break;
            }
            metaDataList.add(metaData);
        }

        return new Result<>(metaDataList);
    }

    /**
     * 获取文件数据
     * @param httpResponse
     * @param taskId
     * @param dataFileName
     * @param offset 用于定位
     * @throws Exception
     */
    @RequestMapping(value = "/filedata")
    public void getMetaData(HttpServletResponse httpResponse,
                            String taskId, String dataFileName, Long offset) throws Exception {
        File dataDir = getTaskDataDir(taskId);
        File appendFile = new File(dataDir, dataFileName);
        if (!appendFile.isFile()) {
            throw new RuntimeException("文件名错误");
        }

        AppendFileReader reader = new AppendFileReader(appendFile);
        if (offset != null) {
            reader.setOffset(offset);
        }

        MetaData metaData = reader.readMetaData();
        if (metaData == null) {
            throw new RuntimeException("获取不到文件metadata信息");
        }
        httpResponse.setHeader("Content-Type", metaData.getContentType());

        byte[] fileData = reader.readFileData();
        if (fileData != null) {
            httpResponse.getOutputStream().write(fileData);
        }
    }

}
