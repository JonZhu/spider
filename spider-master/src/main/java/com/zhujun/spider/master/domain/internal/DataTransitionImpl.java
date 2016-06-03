package com.zhujun.spider.master.domain.internal;

import com.zhujun.spider.master.domain.DataTransition;

public class DataTransitionImpl extends DslActionImpl implements DataTransition {

	private String input;
	private String select;
	private String attr;
	private String regex;
	
	@Override
	public String getInput() {
		return input;
	}

	@Override
	public String getSelect() {
		return select;
	}

	@Override
	public String getAttr() {
		return attr;
	}

	@Override
	public String getRegex() {
		return regex;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setSelect(String select) {
		this.select = select;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

}
