package com.zhujun.spider.master.data.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Singleton;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.zhujun.spider.master.data.db.datasource.DataSourceManager;
import com.zhujun.spider.master.data.db.datasource.DsUtils;
import com.zhujun.spider.master.data.db.datasource.DsUtils.IAction;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;

@Singleton
public class FetchUrlServiceImpl implements IFetchUrlService {

	private final static QueryRunner QUERY_RUNNER = new QueryRunner();
	
	@Override
	public void createFetchUrl(String dataDir, final FetchUrlPo fetchUrl) throws Exception {
		DataSource ds = DataSourceManager.getSpiderDataSource(dataDir);
		DsUtils.doInTrans(ds, new IAction<Void>() {
			@Override
			public Void action(Connection conn) throws Exception {
				createFetchUrlPo(conn, fetchUrl);
				return null;
			}
		});
		
	}
	
	public void createFetchUrl(String dataDir, final List<FetchUrlPo> fetchUrlList) throws Exception {
		if (fetchUrlList == null || fetchUrlList.isEmpty()) {
			return;
		}
		DataSource ds = DataSourceManager.getSpiderDataSource(dataDir);
		DsUtils.doInTrans(ds, new IAction<Void>() {
			@Override
			public Void action(Connection conn) throws Exception {
				for (FetchUrlPo fetchUrlPo : fetchUrlList) {
					createFetchUrlPo(conn, fetchUrlPo);
				}
				return null;
			}
		});
	}
	
	protected void createFetchUrlPo(Connection conn, FetchUrlPo urlPo) throws SQLException {
		urlPo.setInsertTime(new Time(System.currentTimeMillis()));
		urlPo.setModifytime(urlPo.getInsertTime());
		String sql = "insert into fetchurl(url, status, inserttime, modifytime) values(?,?,?,?)";
		long id = QUERY_RUNNER.insert(conn, sql, new ScalarHandler<Long>(), urlPo.getUrl(), 
				urlPo.getStatus(), urlPo.getInsertTime(), urlPo.getModifytime());
		urlPo.setId(id);
	}
	
	

	@Override
	public List<FetchUrlPo> getGiveOutUrls(String dataDir) throws Exception {
		DataSource ds = DataSourceManager.getSpiderDataSource(dataDir);
		return DsUtils.doInTrans(ds, new IAction<List<FetchUrlPo>>() {

			@Override
			public List<FetchUrlPo> action(Connection conn) throws Exception {
				int count = 50;
				List<FetchUrlPo> urlList = new ArrayList<>();
				
				// 查询未下发过的url
				String sql = "select id, url, status, inserttime, modifytime from fetchurl where status = 0 limit ?";
				FetchUrlPoResultHandler resultHandler = new FetchUrlPoResultHandler();
				List<FetchUrlPo> unGiveOutUrls = QUERY_RUNNER.query(conn, sql, resultHandler, count);
				urlList.addAll(unGiveOutUrls);
				
				if (urlList.size() < count) {
					// 数据不够， 查询 下发超过2分钟，但未push结果的url
					Time time = new Time(System.currentTimeMillis());
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(time);
					calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 2);
					time.setTime(calendar.getTimeInMillis());
					sql = "select id, url, status, inserttime, modifytime from fetchurl where status = 2 and modifytime < ? limit ?";
					
					List<FetchUrlPo> unPushUrls = QUERY_RUNNER.query(conn, sql, resultHandler, time, count - urlList.size());
					urlList.addAll(unPushUrls);
				}
				
				if (urlList.size() < count) {
					// 数据不够, 查询 失败的url
					sql = "select id, url, status, inserttime, modifytime from fetchurl where status = 4  limit ?";
					
					List<FetchUrlPo> errorUrls = QUERY_RUNNER.query(conn, sql, resultHandler, count - urlList.size());
					urlList.addAll(errorUrls);
				}
				
				
				
				if (unGiveOutUrls != null && !unGiveOutUrls.isEmpty()) {
					// 修改状态
					StringBuilder updateStatusSql = new StringBuilder("update fetchurl set status = 2 where id in(");
					for (FetchUrlPo fetchUrlPo : unGiveOutUrls) {
						updateStatusSql.append(fetchUrlPo.getId()).append(",");
					}
					updateStatusSql.deleteCharAt(updateStatusSql.length() - 1).append(")");
					
					QUERY_RUNNER.update(conn, updateStatusSql.toString());
				}
				
				
				return urlList;
			}
		});
	}
	
	
	private static class FetchUrlPoResultHandler extends AbstractListHandler<FetchUrlPo> {
		@Override
		protected FetchUrlPo handleRow(ResultSet rs) throws SQLException {
			FetchUrlPo po = new FetchUrlPo();
			po.setId(rs.getLong("id"));
			po.setInsertTime(rs.getTime("inserttime"));
			po.setModifytime(rs.getTime("modifytime"));
			po.setStatus(rs.getInt("status"));
			po.setUrl(rs.getString("url"));
			return po;
		}
		
	}

}
