package com.zhujun.spider.master.dsl;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.zhujun.spider.master.domain.Spider;

/**
 * Xml DSL解析实现
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public class XmlDslParserImpl implements DslParser {

	@Override
	public Spider parse(InputStream inputStream) {
		try {
			Document doc = new SAXReader().read(inputStream);
			
			
			
		} catch (Exception e) {
			throw new RuntimeException("解析Dsl失败", e);
		}
		
		return null;
	}

}
