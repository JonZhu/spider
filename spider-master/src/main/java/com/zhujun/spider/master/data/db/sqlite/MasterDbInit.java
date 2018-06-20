package com.zhujun.spider.master.data.db.sqlite;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

import com.zhujun.spider.master.data.db.sqlite.DsUtils.IAction;

/**
 * Master数据库初始化
 * 
 * @author zhujun
 * @date 2016年6月28日
 *
 */
public class MasterDbInit implements DbInit {

	@Override
	public void init(DataSource dataSource) {
		try {
			DsUtils.doInTrans(dataSource, new IAction<Void>() {

				@Override
				public Void action(Connection conn) throws Exception {
					QueryRunner queryRunner = new QueryRunner();
					
					// 创建spider_task表
					String spiderTaskSql = "create table if not exists spider_task("
							+ "id varchar(100) not null primary key,"
							+ "name varchar(200) not null,"
							+ "author varchar(100),"
							+ "datadir varchar(200) not null,"
							+ "createtime datetime not null,"
							+ "status integer"
							+ ");";
					queryRunner.update(conn, spiderTaskSql);
					
					return null;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("初始化master出错", e);
		}

	}

}
