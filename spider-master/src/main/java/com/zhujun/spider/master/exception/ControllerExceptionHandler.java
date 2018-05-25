package com.zhujun.spider.master.exception;

import com.zhujun.spider.master.controller.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一处理Controller异常
 *
 * @author zhujun
 * @desc ControllerExceptionHandler
 * @time 2018/5/25 17:51
 */
@ControllerAdvice
@ResponseBody
public class ControllerExceptionHandler {
    private final static Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result exceptionHandle(Exception e) {
        log.error(e.getMessage(), e);

        Result result = new Result();
        result.setStatus(500);
        result.setMsg(e.getMessage());

        return result;
    }

}
