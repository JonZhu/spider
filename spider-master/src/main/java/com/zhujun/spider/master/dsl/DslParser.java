package com.zhujun.spider.master.dsl;

import java.io.InputStream;

import com.zhujun.spider.master.domain.Spider;

/**
 * Dsl解析接口
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public interface DslParser {

	Spider parse(InputStream inputStream);
	
}
