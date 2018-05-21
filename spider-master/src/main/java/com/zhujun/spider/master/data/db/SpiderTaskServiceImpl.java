package com.zhujun.spider.master.data.db;

import com.zhujun.spider.master.data.db.datasource.DataSourceManager;
import com.zhujun.spider.master.data.db.datasource.DataSourceType;
import com.zhujun.spider.master.data.db.datasource.DsUtils;
import com.zhujun.spider.master.data.db.datasource.DsUtils.IAction;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo.Status;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.internal.XmlSpider;
import com.zhujun.spider.master.dsl.DslParser;
import com.zhujun.spider.master.dsl.XmlDslParserImpl;
import com.zhujun.spider.master.schedule.IScheduleService;
import com.zhujun.spider.master.util.ReadWriteLockUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

@Service
public class SpiderTaskServiceImpl implements ISpiderTaskService {

	private final static QueryRunner QUERY_RUNNER = new QueryRunner();
	
	/**
	 * master数据库文件
	 */
	private final static String DB_FILE = "./data/master.db";
	static {
		DataSourceManager.regist(DB_FILE, DataSourceType.Master);
	}
	
	@Autowired
	private IScheduleService scheduleService;
	
	@Override
	public void createSpiderTask(final Spider spider) throws Exception {
		
		DataSource ds = DataSourceManager.getDataSource(DB_FILE);
		
		Lock lock = ReadWriteLockUtils.getWriteLock(DB_FILE);
		try {
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
					taskPo.setCreateTime(new Time(System.currentTimeMillis()));
					taskPo.setStatus(Status.RUN);
					insertSpiderTaskPo(conn, taskPo);
					
					// spider 数据目录
					File spiderDataDir = new File(spider.getDataDir());
					if (!spiderDataDir.exists()) {
						spiderDataDir.mkdirs();
					}
					
					// 储存dsl
					if (spider instanceof XmlSpider) {
						XmlSpider xmlSpider = (XmlSpider)spider;
						File dslFile = new File(spiderDataDir, "spiderdsl.xml");
						Writer dslWriter = null;
						try {
							dslWriter = new OutputStreamWriter(new FileOutputStream(dslFile), "UTF-8");
							xmlSpider.getSpiderDslDoc().getRootElement().addAttribute("id", taskPo.getId());
							xmlSpider.getSpiderDslDoc().write(dslWriter);
						} finally {
							IOUtils.closeQuietly(dslWriter);
						}
						
					}
					
					// 初始化spider datasource
					File spiderDbFile = new File(spiderDataDir, "spider.db");
					DataSourceManager.regist(spiderDbFile.getCanonicalPath(), DataSourceType.Spider);
					
					// 启动调度
					scheduleService.startSchedule(taskPo.getId(), spider);
					
					return null;
				}
				
			});
		} finally {
			lock.unlock();
		}
	}

	protected void insertSpiderTaskPo(Connection conn, SpiderTaskPo taskPo) throws SQLException {
		String sql = "insert into spider_task(id, name, author, datadir, createtime, status) values(?,?,?,?,?,?)";
		QUERY_RUNNER.update(conn, sql, taskPo.getId(), taskPo.getName(), taskPo.getAuthor(), 
				taskPo.getDatadir(), taskPo.getCreateTime(), taskPo.getStatus());
	}

	protected int findTaskCountByDatadir(Connection conn, String dataDir) throws SQLException {
		String sql = "select count(*) from spider_task where datadir = ?";
		return QUERY_RUNNER.query(conn, sql, new ScalarHandler<Integer>(), dataDir);
	}

	@Override
	public Page<SpiderTaskPo> findSpiderTaskList(final int pageNo, final int pageSize) throws Exception {
		DataSource ds = DataSourceManager.getDataSource(DB_FILE);
		
		Lock lock = ReadWriteLockUtils.getReadLock(DB_FILE);
		try {
			return DsUtils.doInTrans(ds, new IAction<Page<SpiderTaskPo>>() {
				@Override
				public Page<SpiderTaskPo> action(Connection conn) throws Exception {
					Page<SpiderTaskPo> page = new Page<>();
					
					String countSql = "select count(*) from spider_task";
					int count = QUERY_RUNNER.query(conn, countSql, new ScalarHandler<Integer>());
					
					if (count > 0) {
						String dataSql = "select id, name, author, datadir, createtime, status from spider_task limit ? offset ?";
						List<SpiderTaskPo> data = QUERY_RUNNER.query(conn, dataSql, new SpiderTaskPoResultHandler(), pageSize, (pageNo - 1) * pageSize);
						page.setPageData(data);
					}
					
					page.setDataTotal(count);
					page.setPageNo(pageNo);
					page.setPageSize(pageSize);
					page.setPageTotal(Page.calculatePageTotal(count, pageSize));
					
					return page;
				}
				
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void deleteSpiderTask(final String taskId) throws Exception {
		DataSource ds = DataSourceManager.getDataSource(DB_FILE);
		
		Lock lock = ReadWriteLockUtils.getWriteLock(DB_FILE);
		try {
			DsUtils.doInTrans(ds, new IAction<Void>() {
				@Override
				public Void action(Connection conn) throws Exception {
					String sql = "delete from spider_task where id = ?";
					QUERY_RUNNER.update(conn, sql, taskId);
					return null;
				}
				
			});
		} finally {
			lock.unlock();
		}
		
		// 停止调度
		scheduleService.stopSchedule(taskId);
	}
	
	private static class SpiderTaskPoResultHandler extends AbstractListHandler<SpiderTaskPo> {

		@Override
		protected SpiderTaskPo handleRow(ResultSet rs) throws SQLException {
			SpiderTaskPo po = new SpiderTaskPo();
			po.setId(rs.getString("id"));
			po.setAuthor(rs.getString("author"));
			po.setCreateTime(rs.getTime("createtime"));
			po.setDatadir(rs.getString("datadir"));
			po.setName(rs.getString("name"));
			Object statusObj = null;
			try {
				statusObj = rs.getObject("status");
			} catch (Exception e) {
			}
			if (statusObj != null) {
				po.setStatus((int)statusObj);
			}
			
			return po;
		}
		
	}

	@Override
	public List<SpiderTaskPo> findAllScheduleSpiderTask() throws Exception {
		DataSource ds = DataSourceManager.getDataSource(DB_FILE);
		
		Lock lock = ReadWriteLockUtils.getReadLock(DB_FILE);
		try {
			return DsUtils.doInTrans(ds, new IAction<List<SpiderTaskPo>>() {
				@Override
				public List<SpiderTaskPo> action(Connection conn) throws Exception {
					String dataSql = "select id, name, author, datadir, createtime, status from spider_task where status = 1";
					return QUERY_RUNNER.query(conn, dataSql, new SpiderTaskPoResultHandler());
				}
				
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void pauseTask(final String taskId) throws Exception {
		DataSource ds = DataSourceManager.getDataSource(DB_FILE);
		Lock lock = ReadWriteLockUtils.getWriteLock(DB_FILE);
		
		try {
			DsUtils.doInTrans(ds, new IAction<Void>() {
				@Override
				public Void action(Connection conn) throws Exception {
					SpiderTaskPo task = getTaskById(conn, taskId);
					if (task == null) {
						throw new Exception("任务不存在");
					}
					
					if (task.getStatus() != Status.RUN) {
						throw new Exception("任务不能暂停");
					}
					
					scheduleService.pauseSchedule(taskId);
					
					updateTaskStatus(conn, taskId, Status.PAUSED);
					return null;
				}
				
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void resumeTask(final String taskId) throws Exception {
		DataSource ds = DataSourceManager.getDataSource(DB_FILE);
		Lock lock = ReadWriteLockUtils.getWriteLock(DB_FILE);
		
		try {
			DsUtils.doInTrans(ds, new IAction<Void>() {
				@Override
				public Void action(Connection conn) throws Exception {
					SpiderTaskPo task = getTaskById(conn, taskId);
					if (task == null) {
						throw new Exception("任务不存在");
					}
					
					if (task.getStatus() != Status.PAUSED) {
						throw new Exception("任务不能恢复");
					}
					
					// 加载spider dsl
					FileInputStream dslInputStream = null;
					Spider spider = null;
					
					try {
						final DslParser dslParser = new XmlDslParserImpl(); // xml dsl解析
						File dslFile = new File(task.getDatadir(), "spiderdsl.xml");
						dslInputStream = new FileInputStream(dslFile);
						spider = dslParser.parse(dslInputStream);
					} finally {
						IOUtils.closeQuietly(dslInputStream);
					}
					
					// 恢复调度
					scheduleService.resumeSchedule(taskId, spider);
					
					updateTaskStatus(conn, taskId, Status.RUN);
					return null;
				}
				
			});
		} finally {
			lock.unlock();
		}
		
	}
	
	protected SpiderTaskPo getTaskById(Connection conn, String taskId) throws SQLException {
		String sql = "select id, name, author, datadir, createtime, status from spider_task where id=?";
		List<SpiderTaskPo> list = QUERY_RUNNER.query(conn, sql, new SpiderTaskPoResultHandler(), taskId);
		return list == null || list.isEmpty() ? null : list.get(0);
	}
	
	
	protected int updateTaskStatus(Connection conn, String taskId, int status) throws SQLException {
		String sql = "update spider_task set status = ? where id=?";
		return QUERY_RUNNER.update(conn, sql, status, taskId);
	}

}
