package com.zhujun.spider.master.data.db.datasource;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

import com.zhujun.spider.master.data.db.datasource.DsUtils.IAction;

/**
 * Spider数据库初始化
 * 
 * @author zhujun
 * @date 2016年6月28日
 *
 */
public class SpiderDbInit implements DbInit {

	@Override
	public void init(DataSource dataSource) {
		try {
			DsUtils.doInTrans(dataSource, new IAction<Void>() {

				@Override
				public Void action(Connection conn) throws Exception {
					QueryRunner queryRunner = new QueryRunner();
					
					// 创建fetchurl表
					String spiderTaskSql = "create table if not exists fetchurl("
							+ "id varchar(100) not null primary key,"
							+ "url varchar(500) not null,"
							+ "status int not null,"
							+ "inserttime datetime not null,"
							+ "modifytime datetime not null"
							+ ");";
					queryRunner.update(conn, spiderTaskSql);
					
					return null;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("初始化fetchurl出错", e);
		}

	}

}
