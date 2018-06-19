package com.zhujun.spider.master.data.db;

import com.zhujun.spider.master.data.db.dao.FetchUrlDao;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.data.db.sqlite.DataSourceManager;
import com.zhujun.spider.master.data.db.sqlite.DsUtils;
import com.zhujun.spider.master.data.db.sqlite.DsUtils.IAction;
import com.zhujun.spider.master.util.ReadWriteLockUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.locks.Lock;

@Service
public class FetchUrlServiceImpl implements IFetchUrlService {

	private final static Logger LOG = LoggerFactory.getLogger(FetchUrlServiceImpl.class);
	
	private final static QueryRunner QUERY_RUNNER = new QueryRunner();

	@Autowired
	private FetchUrlDao fetchUrlDao;
	
	@Override
	public void createFetchUrl(SpiderTaskPo task, final FetchUrlPo fetchUrl) throws Exception {
		if (!fetchUrlDao.existByUrl(task, fetchUrl.getUrl())) {
			fetchUrlDao.insertFetchUrl(task, fetchUrl);
		}
	}
	
	public void createFetchUrl(SpiderTaskPo task, final List<FetchUrlPo> fetchUrlList) throws Exception {
		if (fetchUrlList == null || fetchUrlList.isEmpty()) {
			return;
		}

		for (FetchUrlPo fetchUrlPo : fetchUrlList) {
			createFetchUrl(task, fetchUrlPo);
		}
	}


	@Override
	public List<FetchUrlPo> getGiveOutUrls(SpiderTaskPo task) throws Exception {
		int count = 50;
		List<FetchUrlPo> urlList = new ArrayList<>();

		// 查询未下发过的url
		List<FetchUrlPo> unGiveOutUrls = fetchUrlDao.findFetchurl(task, FetchUrlPo.STATUS_INIT, null, count);
		if (unGiveOutUrls != null) {
			urlList.addAll(unGiveOutUrls);
		}

		if (urlList.size() < count) {
			// 数据不够， 查询 下发超过2分钟，但未push结果的url
			Date time = new Time(System.currentTimeMillis());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 2);
			time.setTime(calendar.getTimeInMillis());

			List<FetchUrlPo> unPushUrls = fetchUrlDao.findFetchurl(task, FetchUrlPo.STATUS_PUSHED, time, count - urlList.size());
			urlList.addAll(unPushUrls);
		}

		if (urlList.size() < count) {
			// 数据不够, 查询 失败的url
			List<FetchUrlPo> errorUrls = fetchUrlDao.findFetchurl(task, FetchUrlPo.STATUS_ERROR, null, count - urlList.size());
			urlList.addAll(errorUrls);
		}



		if (unGiveOutUrls != null && !unGiveOutUrls.isEmpty()) {
			// 修改状态
			List<String> idList = new ArrayList<>();
			for (FetchUrlPo fetchUrlPo : unGiveOutUrls) {
				idList.add(fetchUrlPo.getId());
			}

			fetchUrlDao.updateFetchUrl(task, idList, FetchUrlPo.STATUS_PUSHED, new Time(System.currentTimeMillis()));
		}


		return urlList;
	}

	@Override
	public boolean existUnFetchUrlInAction(SpiderTaskPo task, final String actionId) throws Exception {
		List<Integer> statusList = Arrays.asList(FetchUrlPo.STATUS_INIT, FetchUrlPo.STATUS_ERROR, FetchUrlPo.STATUS_PUSHED);
		return fetchUrlDao.existByAction(task, actionId, statusList);
	}

	@Override
	public int setFetchUrlStatus(SpiderTaskPo task, final String urlId, final int status, final Date time) throws Exception {
		return fetchUrlDao.updateFetchUrl(task, Arrays.asList(urlId), status, time);
	}

}
