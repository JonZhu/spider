package com.zhujun.spider.master.domain.internal;

import org.dom4j.Document;

import com.zhujun.spider.master.domain.Spider;

public class XmlSpider extends DslParentActionImpl implements Spider {

	private String author;
	
	private Document spiderDslDoc;
	
	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	public Document getSpiderDslDoc() {
		return spiderDslDoc;
	}

	public void setSpiderDslDoc(Document spiderDslDoc) {
		this.spiderDslDoc = spiderDslDoc;
	}

}
