package com.zhujun.spider.master.data.db.po;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * html抽取任务
 *
 * @author zhujun
 * @desc HtmlExtractTaskPo
 * @time 2018/7/2 9:45
 */
@Document(collection = "extract_html_task")
public class HtmlExtractTaskPo {
    public static interface Status {
        int NEW = 0;

        int RUN = 1;

        int PAUSED = 4;

        /**
         * 正常完成
         */
        int COMPLETE = 6;

        int DELETED = 7;

        /**
         * 出错，错误结束
         */
        int ERROR = 9;
    }


    @Id
    private String id;

    /**
     * 自定义任务名称
     */
    private String taskName;

    /**
     * 源数据目录
     */
    private String srcDataDir;

    /**
     * html抽取配置
     */
    private String extractConfig;

    /**
     * 用于抽取结果存储
     */
    private String mongoDbName;

    /**
     * 用于抽取结果存储
     */
    private String mongoCollectionName;

    /**
     * 任务状态
     */
    private int status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 抽取执行成功数量
     */
    private int successCount;

    /**
     * 抽取执行失败数量
     */
    private int failCount;

    /**
     * 状态为错误时，错误详细信息
     */
    private String errorInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getSrcDataDir() {
        return srcDataDir;
    }

    public void setSrcDataDir(String srcDataDir) {
        this.srcDataDir = srcDataDir;
    }

    public String getExtractConfig() {
        return extractConfig;
    }

    public void setExtractConfig(String extractConfig) {
        this.extractConfig = extractConfig;
    }

    public String getMongoDbName() {
        return mongoDbName;
    }

    public void setMongoDbName(String mongoDbName) {
        this.mongoDbName = mongoDbName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public String getMongoCollectionName() {
        return mongoCollectionName;
    }

    public void setMongoCollectionName(String mongoCollectionName) {
        this.mongoCollectionName = mongoCollectionName;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}
