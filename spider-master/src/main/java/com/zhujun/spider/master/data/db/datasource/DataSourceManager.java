package com.zhujun.spider.master.data.db.datasource;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * 数据库连接池管理
 * 
 * @author zhujun
 * @date 2016年6月28日
 *
 */
public class DataSourceManager {

	private final static Map<String, DataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<>();
	
	private final static Map<DataSourceType, DbInit> DBINIT_MAP = new HashMap<>();
	static {
		DBINIT_MAP.put(DataSourceType.Master, new MasterDbInit());
		DBINIT_MAP.put(DataSourceType.Spider, new SpiderDbInit());
	}
	
	/**
	 * 注册
	 * 
	 * @author zhujun
	 * @date 2016年6月28日
	 *
	 * @param dbFile
	 * @param type
	 */
	synchronized public static void regist(String dbFile, DataSourceType type) {
		if (DATA_SOURCE_MAP.containsKey(dbFile)) {
			throw new RuntimeException("DataSource ["+ dbFile +"]已经存在");
		}
		
		if (!DBINIT_MAP.containsKey(type)) {
			throw new RuntimeException("未找到数据源类型 ["+ type +"]的初始化程序");
		}
		
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.sqlite.JDBC");
		dataSource.setUrl("jdbc:sqlite:" + dbFile);
		
		DBINIT_MAP.get(type).init(dataSource); // 执行初始化
		DATA_SOURCE_MAP.put(dbFile, dataSource);
	}
	
	public static DataSource getDataSource(String dbFile) {
		return DATA_SOURCE_MAP.get(dbFile);
	}
	
	
	/**
	 * 删除
	 * 
	 * @author zhujun
	 * @date 2016年6月28日
	 *
	 * @param dbFile
	 */
	synchronized public static void remove(String dbFile) {
		DataSource dataSource = DATA_SOURCE_MAP.get(dbFile);
		if (dataSource != null) {
			try {
				((BasicDataSource)dataSource).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
}
