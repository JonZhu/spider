package com.zhujun.spider.master.data.db;

import com.zhujun.spider.master.data.db.datasource.DataSourceManager;
import com.zhujun.spider.master.data.db.datasource.DsUtils;
import com.zhujun.spider.master.data.db.datasource.DsUtils.IAction;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.util.ReadWriteLockUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

@Service
public class FetchUrlServiceImpl implements IFetchUrlService {

	private final static Logger LOG = LoggerFactory.getLogger(FetchUrlServiceImpl.class);
	
	private final static QueryRunner QUERY_RUNNER = new QueryRunner();
	
	@Override
	public void createFetchUrl(String dataDir, final FetchUrlPo fetchUrl) throws Exception {
		DataSource ds = DataSourceManager.getSpiderDataSource(dataDir);
		
		Lock lock = ReadWriteLockUtils.getWriteLock(dataDir);
		try {
			DsUtils.doInTrans(ds, new IAction<Void>() {
				@Override
				public Void action(Connection conn) throws Exception {
					createFetchUrlPo(conn, fetchUrl);
					return null;
				}
			});
		} finally {
			lock.unlock();
		}
		
	}
	
	public void createFetchUrl(String dataDir, final List<FetchUrlPo> fetchUrlList) throws Exception {
		if (fetchUrlList == null || fetchUrlList.isEmpty()) {
			return;
		}
		DataSource ds = DataSourceManager.getSpiderDataSource(dataDir);
		
		Lock lock = ReadWriteLockUtils.getWriteLock(dataDir);
		try {
			DsUtils.doInTrans(ds, new IAction<Void>() {
				@Override
				public Void action(Connection conn) throws Exception {
					for (FetchUrlPo fetchUrlPo : fetchUrlList) {
						createFetchUrlPo(conn, fetchUrlPo);
					}
					return null;
				}
			});
		} finally {
			lock.unlock();
		}
	}
	
	protected void createFetchUrlPo(Connection conn, FetchUrlPo urlPo) throws SQLException {
		// check exist
		String existSql = "select id from fetchurl where url = ? limit 1";
		Integer existId = QUERY_RUNNER.query(conn, existSql, new ScalarHandler<Integer>(), urlPo.getUrl());
		if (existId == null) {
			urlPo.setInsertTime(new Time(System.currentTimeMillis()));
			urlPo.setModifytime(urlPo.getInsertTime());
			String sql = "insert into fetchurl(url, status, inserttime, modifytime, actionid) values(?,?,?,?,?)";
			int id = QUERY_RUNNER.insert(conn, sql, new ScalarHandler<Integer>(), urlPo.getUrl(), 
					urlPo.getStatus(), urlPo.getInsertTime(), urlPo.getModifytime(), urlPo.getActionId());
			urlPo.setId(id);
		}
	}
	

	@Override
	public List<FetchUrlPo> getGiveOutUrls(String dataDir) throws Exception {
		DataSource ds = DataSourceManager.getSpiderDataSource(dataDir);
		
		LOG.debug(dataDir);
		Lock lock = ReadWriteLockUtils.getWriteLock(dataDir);
		try {
			return DsUtils.doInTrans(ds, new IAction<List<FetchUrlPo>>() {
	
				@Override
				public List<FetchUrlPo> action(Connection conn) throws Exception {
					int count = 50;
					List<FetchUrlPo> urlList = new ArrayList<>();
					
					// 查询未下发过的url
					String sql = "select id, url, status, inserttime, modifytime, actionid from fetchurl where status = 0 limit ?";
					FetchUrlPoResultHandler resultHandler = new FetchUrlPoResultHandler();
					List<FetchUrlPo> unGiveOutUrls = QUERY_RUNNER.query(conn, sql, resultHandler, count);
					if (unGiveOutUrls != null) {
						urlList.addAll(unGiveOutUrls);
					}
					
					if (urlList.size() < count) {
						// 数据不够， 查询 下发超过2分钟，但未push结果的url
						Time time = new Time(System.currentTimeMillis());
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(time);
						calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 2);
						time.setTime(calendar.getTimeInMillis());
						sql = "select id, url, status, inserttime, modifytime, actionid from fetchurl where status = 2 and modifytime < ? limit ?";
						
						List<FetchUrlPo> unPushUrls = QUERY_RUNNER.query(conn, sql, resultHandler, time, count - urlList.size());
						urlList.addAll(unPushUrls);
					}
					
					if (urlList.size() < count) {
						// 数据不够, 查询 失败的url
						sql = "select id, url, status, inserttime, modifytime, actionid from fetchurl where status = 4  limit ?";
						
						List<FetchUrlPo> errorUrls = QUERY_RUNNER.query(conn, sql, resultHandler, count - urlList.size());
						urlList.addAll(errorUrls);
					}
					
					
					
					if (unGiveOutUrls != null && !unGiveOutUrls.isEmpty()) {
						// 修改状态
						StringBuilder updateStatusSql = new StringBuilder("update fetchurl set status = ?, modifytime = ? where id in(");
						for (FetchUrlPo fetchUrlPo : unGiveOutUrls) {
							updateStatusSql.append(fetchUrlPo.getId()).append(",");
						}
						updateStatusSql.deleteCharAt(updateStatusSql.length() - 1).append(")");
						
						QUERY_RUNNER.update(conn, updateStatusSql.toString(), FetchUrlPo.STATUS_PUSHED, new Time(System.currentTimeMillis()));
					}
					
					
					return urlList;
				}
			});
		} finally {
			lock.unlock();
		}
	}
	
	
	private static class FetchUrlPoResultHandler extends AbstractListHandler<FetchUrlPo> {
		@Override
		protected FetchUrlPo handleRow(ResultSet rs) throws SQLException {
			FetchUrlPo po = new FetchUrlPo();
			po.setId(rs.getInt("id"));
			po.setInsertTime(rs.getTime("inserttime"));
			po.setModifytime(rs.getTime("modifytime"));
			po.setStatus(rs.getInt("status"));
			po.setUrl(rs.getString("url"));
			po.setActionId(rs.getString("actionid"));
			return po;
		}
		
	}


	@Override
	public boolean existUnFetchUrlInAction(String dataDir, final String actionId) throws Exception {
		DataSource ds = DataSourceManager.getSpiderDataSource(dataDir);
		Lock lock = ReadWriteLockUtils.getReadLock(dataDir);
		try {
			return DsUtils.doInTrans(ds, new IAction<Boolean>() {
				
				@Override
				public Boolean action(Connection conn) throws Exception {
					String sql = "select id from fetchurl where actionid = ? and status in (?,?,?) limit 1";
					Integer id = QUERY_RUNNER.query(conn, sql, new ScalarHandler<Integer>(), 
							actionId, FetchUrlPo.STATUS_INIT, FetchUrlPo.STATUS_ERROR, FetchUrlPo.STATUS_PUSHED);
					return id != null;
				}
				
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int setFetchUrlStatus(String dataDir, final Integer urlId, final int status, final Date time) throws Exception {
		DataSource ds = DataSourceManager.getSpiderDataSource(dataDir);
		Lock lock = ReadWriteLockUtils.getWriteLock(dataDir);
		try {
			return DsUtils.doInTrans(ds, new IAction<Integer>() {
	
				@Override
				public Integer action(Connection conn) throws Exception {
					String sql = "update fetchurl set status = ?, modifytime = ? where id = ?";
					return QUERY_RUNNER.update(conn, sql, status, new Time(time.getTime()), urlId);
				}
				
			});
		} finally {
			lock.unlock();
		}
		
	}

}
