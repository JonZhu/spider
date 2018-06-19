package com.zhujun.spider.master.util;

import java.util.UUID;

/**
 * @author zhujun
 * @desc UuidUtil
 * @time 2018/6/19 15:06
 */
public class UuidUtil {
    public static String create() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
