package com.zhujun.spider.master.data.db.datasource;

import javax.sql.DataSource;

/**
 * 数据库初始化，如建表
 * 
 * @author zhujun
 * @date 2016年6月28日
 *
 */
public interface DbInit {

	void init(DataSource dataSource);
	
}
