package com.zhujun.spider.master.data.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;

import com.zhujun.spider.master.data.db.datasource.DataSourceManager;
import com.zhujun.spider.master.data.db.datasource.DataSourceType;
import com.zhujun.spider.master.data.db.datasource.DsUtils;
import com.zhujun.spider.master.data.db.datasource.DsUtils.IAction;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.internal.XmlSpider;

@Singleton
public class SpiderTaskServiceImpl implements ISpiderTaskService {

	private final static QueryRunner QUERY_RUNNER = new QueryRunner();
	
	/**
	 * master数据库文件
	 */
	private final static String DB_FILE = "./data/master.db";
	static {
		DataSourceManager.regist(DB_FILE, DataSourceType.Master);
	}
	
	@Override
	public void createSpiderTask(final Spider spider) throws Exception {
		
		DataSource ds = DataSourceManager.getDataSource(DB_FILE);
		DsUtils.doInTrans(ds, new IAction<Void>() {
			@Override
			public Void action(Connection conn) throws Exception {
				// 校验是否已有datadir相同的任务
				if (findTaskCountByDatadir(conn, spider.getDataDir()) > 0) {
					throw new Exception("datadir被其它任务占用");
				}
				
				SpiderTaskPo taskPo = new SpiderTaskPo();
				taskPo.setId(UUID.randomUUID().toString());
				taskPo.setName(spider.getName());
				taskPo.setAuthor(spider.getAuthor());
				taskPo.setDatadir(spider.getDataDir());
				taskPo.setCreateTime(new Date(System.currentTimeMillis()));
				insertSpiderTaskPo(conn, taskPo);
				
				// spider 数据目录
				File spiderDataDir = new File(spider.getDataDir());
				if (!spiderDataDir.exists()) {
					spiderDataDir.mkdirs();
				}
				
				// 储存dsl
				if (spider instanceof XmlSpider) {
					File dslFile = new File(spiderDataDir, "spiderdsl.xml");
					Writer dslWriter = null;
					try {
						dslWriter = new OutputStreamWriter(new FileOutputStream(dslFile));
						((XmlSpider) spider).getSpiderDslDoc().write(dslWriter);
					} finally {
						IOUtils.closeQuietly(dslWriter);
					}
					
				}
				
				// 初始化spider datasource
				File spiderDbFile = new File(spiderDataDir, "spider.db");
				DataSourceManager.regist(spiderDbFile.getCanonicalPath(), DataSourceType.Spider);
				
				return null;
			}
			
		});
	}

	protected void insertSpiderTaskPo(Connection conn, SpiderTaskPo taskPo) throws SQLException {
		String sql = "insert into spider_task(id, name, author, datadir, createtime) value(?,?,?,?,?)";
		QUERY_RUNNER.update(conn, sql, taskPo.getId(), taskPo.getName(), taskPo.getAuthor(), taskPo.getDatadir(), taskPo.getCreateTime());
	}

	protected int findTaskCountByDatadir(Connection conn, String dataDir) throws SQLException {
		String sql = "select count(*) from spider_task where datadir = ?";
		return QUERY_RUNNER.query(conn, sql, new ScalarHandler<Integer>(), dataDir);
	}

	@Override
	public Page<SpiderTaskPo> findSpiderTaskList(final int pageNo, final int pageSize) throws Exception {
		DataSource ds = DataSourceManager.getDataSource(DB_FILE);
		return DsUtils.doInTrans(ds, new IAction<Page<SpiderTaskPo>>() {
			@Override
			public Page<SpiderTaskPo> action(Connection conn) throws Exception {
				Page<SpiderTaskPo> page = new Page<>();
				
				String countSql = "select count(*) from spider_task";
				int count = QUERY_RUNNER.query(conn, countSql, new ScalarHandler<Integer>());
				
				if (count > 0) {
					String dataSql = "select id, name, author, datadir, createtime from spider_task limit ? offset ?";
					List<SpiderTaskPo> data = QUERY_RUNNER.query(dataSql, new SpiderTaskPoResultHandler(), pageSize, (pageNo - 1) * pageSize);
					page.setPageData(data);
				}
				
				page.setDataTotal(count);
				page.setPageNo(pageNo);
				page.setPageSize(pageSize);
				page.setPageTotal(Page.calculatePageTotal(count, pageSize));
				
				return page;
			}
			
		});

	}

	@Override
	public void deleteSpiderTask(final String taskId) throws Exception {
		DataSource ds = DataSourceManager.getDataSource(DB_FILE);
		DsUtils.doInTrans(ds, new IAction<Void>() {
			@Override
			public Void action(Connection conn) throws Exception {
				String sql = "delete from spider_task where id = ?";
				QUERY_RUNNER.update(conn, sql, taskId);
				return null;
			}
			
		});
	}
	
	private static class SpiderTaskPoResultHandler extends AbstractListHandler<SpiderTaskPo> {

		@Override
		protected SpiderTaskPo handleRow(ResultSet rs) throws SQLException {
			SpiderTaskPo po = new SpiderTaskPo();
			po.setId(rs.getString("id"));
			po.setAuthor(rs.getString("author"));
			po.setCreateTime(rs.getDate("createtime"));
			po.setDatadir(rs.getString("datadir"));
			po.setName(rs.getString("name"));
			
			return po;
		}

	
		
	}

}
