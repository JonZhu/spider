package com.zhujun.spider.master.domain.internal;

import org.dom4j.Document;

import com.zhujun.spider.master.domain.Spider;

public class XmlSpider extends DslParentActionImpl implements Spider {

	private String name;
	
	private String author;
	
	private String dataDir;
	
	/**
	 * appendfile、eachfile
	 */
	private String dataWriterType = "appendfile";
	
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

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDataWriterType() {
		return dataWriterType;
	}

	public void setDataWriterType(String dataWriterType) {
		this.dataWriterType = dataWriterType;
	}

}
