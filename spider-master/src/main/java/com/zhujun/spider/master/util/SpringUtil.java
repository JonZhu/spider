package com.zhujun.spider.master.util;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author zhujun
 * @desc SpringUtil
 * @time 2018/5/18 9:33
 */
public class SpringUtil {

    private static ConfigurableApplicationContext CONTEXT;

    public static ConfigurableApplicationContext getContext() {
        return CONTEXT;
    }

}
