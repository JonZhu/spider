package com.zhujun.spider.master.data.db.datasource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.SynchronousMode;
import org.sqlite.SQLiteDataSource;

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
		
//		BasicDataSource dataSource = new BasicDataSource();
//		dataSource.setDriverClassName("org.sqlite.JDBC");
//		dataSource.setUrl("jdbc:sqlite:" + dbFile);
//		dataSource.setInitialSize(0);
//		dataSource.setMaxTotal(10);
		
		SQLiteConfig config = new SQLiteConfig();
		config.setSynchronous(SynchronousMode.OFF);
		SQLiteDataSource dataSource = new SQLiteDataSource(config);
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
		DATA_SOURCE_MAP.remove(dbFile);
	}
	
	
	/**
	 * 获取 spider运行时数据源, 没有则注册一个
	 * @author zhujun
	 * @date 2016年7月4日
	 *
	 * @param dataDir spider任务数据目录
	 * @return
	 */
	public static DataSource getSpiderDataSource(String dataDir) {
		File dbFile = new File(dataDir, "spider.db");
		String dbFilePath = null;
		try {
			dbFilePath = dbFile.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException("获取db路径失败", e);
		}
		
		DataSource ds = null;
		synchronized (DataSourceManager.class) {
			ds = DataSourceManager.getDataSource(dbFilePath);
			if (ds == null) {
				DataSourceManager.regist(dbFilePath, DataSourceType.Spider);
				ds = DataSourceManager.getDataSource(dbFilePath);
			}
		}
		
		return ds;
		
	}
	
	
}
