package com.zhujun.spider.master.data.db.sqlite;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

/**
 * 数据库工具
 * 
 * @author zhujun
 * @date 2016年6月28日
 *
 */
public class DsUtils {

	/**
	 * 在事务中执行
	 * 
	 * <p>获取连接并打开事务, 执行action, 提交或回滚事务, 关闭连接</p>
	 * 
	 * @author zhujun
	 * @date 2016年6月28日
	 *
	 * @param ds
	 * @param action
	 * @return
	 * @throws Exception
	 */
	public static <T> T doInTrans(DataSource ds, IAction<T> action) throws Exception {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			conn.setAutoCommit(false);
			T result = action.action(conn);
			DbUtils.commitAndClose(conn);
			return result;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}
	}
	
	
	public static interface IAction<T> {
		T action(Connection conn) throws Exception;
	}
	
}
