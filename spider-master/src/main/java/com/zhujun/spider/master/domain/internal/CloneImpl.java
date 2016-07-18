package com.zhujun.spider.master.domain.internal;

import com.zhujun.spider.master.domain.Clone;

public class CloneImpl extends DslActionImpl implements Clone {

	private boolean allowCss = true;
	
	private boolean allowJs = true;
	
	private boolean allowImage = true;
	
	private String[] seeds;
	
	private String[] hosts;

	public boolean isAllowCss() {
		return allowCss;
	}

	public void setAllowCss(boolean allowCss) {
		this.allowCss = allowCss;
	}

	public boolean isAllowJs() {
		return allowJs;
	}

	public void setAllowJs(boolean allowJs) {
		this.allowJs = allowJs;
	}

	public boolean isAllowImage() {
		return allowImage;
	}

	public void setAllowImage(boolean allowImage) {
		this.allowImage = allowImage;
	}

	public String[] getSeeds() {
		return seeds;
	}

	public void setSeeds(String[] seeds) {
		this.seeds = seeds;
	}

	public String[] getHosts() {
		return hosts;
	}

	public void setHosts(String[] hosts) {
		this.hosts = hosts;
	}
	

}
