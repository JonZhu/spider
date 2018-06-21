package com.zhujun.spider.master.exception;

/**
 * 异常忽略
 *
 * <p>某些接口需要忽略一些异常,特别是批量操作接口</p>
 *
 * @author zhujun
 * @desc ExceptionIgnore
 * @time 2018/6/21 11:38
 */
public interface ExceptionIgnore {
    boolean isIgnore(Exception e);
}
