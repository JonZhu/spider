package com.zhujun.spider.master.data.db.sqlite;

import com.zhujun.spider.master.data.db.Page;
import com.zhujun.spider.master.data.db.dao.SpiderTaskDao;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhujun
 * @desc SpiderTaskDaoSqliteImpl
 * @time 2018/6/19 14:11
 */
public class SpiderTaskDaoSqliteImpl implements SpiderTaskDao {
    private final static QueryRunner QUERY_RUNNER = new QueryRunner();

    /**
     * master数据库文件
     */
    private final static String DB_FILE = "./data/master.db";
    static {
        DataSourceManager.regist(DB_FILE, DataSourceType.Master);
    }


    @Override
    public int countByDatadir(String dataDir) {
        String sql = "select count(*) from spider_task where datadir = ?";
        try {
            return QUERY_RUNNER.query(getConnection(), sql, new ScalarHandler<Integer>(), dataDir);
        } catch (SQLException e) {
            throw new RuntimeException("countByDatadir出错", e);
        }
    }

    @Override
    public void insertSpiderTaskPo(SpiderTaskPo taskPo) {
        String sql = "insert into spider_task(id, name, author, datadir, createtime, status) values(?,?,?,?,?,?)";
        try {
            QUERY_RUNNER.update(getConnection(), sql, taskPo.getId(), taskPo.getName(), taskPo.getAuthor(),
                    taskPo.getDatadir(), taskPo.getCreateTime(), taskPo.getStatus());
        } catch (SQLException e) {
            throw new RuntimeException("保存spider task出错", e);
        }
    }

    private Connection getConnection() {
        // todo
        return null;
    }

    @Override
    public Page<SpiderTaskPo> pagingTask(int pageNo, int pageSize) {
        Page<SpiderTaskPo> page = new Page<>();

        Connection conn = getConnection();
        String countSql = "select count(*) from spider_task";
        int count = 0;
        try {
            count = QUERY_RUNNER.query(conn, countSql, new ScalarHandler<Integer>());
        } catch (SQLException e) {
            throw new RuntimeException("查询数量出错", e);
        }

        if (count > 0) {
            String dataSql = "select id, name, author, datadir, createtime, status from spider_task limit ? offset ?";
            List<SpiderTaskPo> data = null;
            try {
                data = QUERY_RUNNER.query(conn, dataSql, new SpiderTaskPoResultHandler(), pageSize, (pageNo - 1) * pageSize);
            } catch (SQLException e) {
                throw new RuntimeException("查询分页数据出错", e);
            }
            page.setPageData(data);
        }

        page.setDataTotal(count);
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        page.setPageTotal(Page.calculatePageTotal(count, pageSize));

        return page;
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
    public void deleteTask(String taskId) {
        String sql = "delete from spider_task where id = ?";
        try {
            QUERY_RUNNER.update(getConnection(), sql, taskId);
        } catch (SQLException e) {
            throw new RuntimeException("删除任务出错", e);
        }
    }

    @Override
    public List<SpiderTaskPo> findScheduledTask() {
        String dataSql = "select id, name, author, datadir, createtime, status from spider_task where status = 1";
        try {
            return QUERY_RUNNER.query(getConnection(), dataSql, new SpiderTaskPoResultHandler());
        } catch (SQLException e) {
            throw new RuntimeException("删除任务出错", e);
        }
    }

    @Override
    public SpiderTaskPo getTaskById(String taskId) {
        String sql = "select id, name, author, datadir, createtime, status from spider_task where id=?";
        List<SpiderTaskPo> list = null;
        try {
            list = QUERY_RUNNER.query(getConnection(), sql, new SpiderTaskPoResultHandler(), taskId);
        } catch (SQLException e) {
            throw new RuntimeException("查询任务出错", e);
        }
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Override
    public int updateTaskStatus(String taskId, int status) {
        String sql = "update spider_task set status = ? where id=?";
        try {
            return QUERY_RUNNER.update(getConnection(), sql, status, taskId);
        } catch (SQLException e) {
            throw new RuntimeException("修改任务状态出错", e);
        }
    }
}
