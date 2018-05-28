package com.zhujun.spider.master.controller.vo;

import java.util.Date;

/**
 * 数据文件信息
 *
 * @author zhujun
 * @desc DataFileVo
 * @time 2018/5/28 11:02
 */
public class DataFileVo {
    /**
     * 名称, 相对于数据目录
     */
    private String name;

    private long size;

    private Date createTime;

    private Date modifyTime;

    /**
     * 任务id
     */
    private String taskId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
