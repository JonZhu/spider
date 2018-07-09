package com.zhujun.spider.master.data.db;

import com.zhujun.spider.master.data.db.dao.FetchUrlDao;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.exception.ExceptionIgnore;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FetchUrlServiceImpl implements IFetchUrlService {

	private final static Logger LOG = LoggerFactory.getLogger(FetchUrlServiceImpl.class);
	
	private final static QueryRunner QUERY_RUNNER = new QueryRunner();

	private final static ConcurrentHashMap<String, Lock> GET_GIVEOUT_URLS_LOCK_MAP = new ConcurrentHashMap<>();

	@Autowired
	private FetchUrlDao fetchUrlDao;
	
	@Override
	public int createFetchUrl(SpiderTaskPo task, final FetchUrlPo fetchUrl) throws Exception {
		return createFetchUrl(task, fetchUrl, null);
	}

	public int createFetchUrl(SpiderTaskPo task, final FetchUrlPo fetchUrl, ExceptionIgnore exceptionIgnore) throws Exception {
		int insertCount = 0;
		if (!fetchUrlDao.existByUrl(task, fetchUrl.getUrl())) {
			if (exceptionIgnore == null) {
				fetchUrlDao.insertFetchUrl(task, fetchUrl);
			} else {
				try {
					fetchUrlDao.insertFetchUrl(task, fetchUrl);
					insertCount++;
				} catch (Exception e) {
					if (exceptionIgnore.isIgnore(e)) {
						LOG.warn("忽略的异常", e);
					} else {
						throw e;
					}
				}
			}
		}
		return insertCount;
	}
	
	public int createFetchUrl(SpiderTaskPo task, final List<FetchUrlPo> fetchUrlList) throws Exception {
		return createFetchUrl(task, fetchUrlList, null);
	}

	public int createFetchUrl(SpiderTaskPo task, final List<FetchUrlPo> fetchUrlList, ExceptionIgnore exceptionIgnore) throws Exception {
		if (fetchUrlList == null || fetchUrlList.isEmpty()) {
			return 0;
		}

		int insertCount = 0;
		for (FetchUrlPo fetchUrlPo : fetchUrlList) {
			insertCount += createFetchUrl(task, fetchUrlPo, exceptionIgnore);
		}
		return insertCount;
	}


	@Override
	public List<FetchUrlPo> getGiveOutUrls(SpiderTaskPo task) throws Exception {
	    long startTime = System.currentTimeMillis();
		int count = 100;
		List<FetchUrlPo> urlList = new ArrayList<>();

		Lock lock = getGiveOutUrlsLock(task);
        lock.lock(); // 查询和更新状态需要加锁
		try {
			// 查询未下发过的url
			List<FetchUrlPo> unGiveOutUrls = fetchUrlDao.findFetchurl(task, FetchUrlPo.STATUS_INIT, null, count);
			if (unGiveOutUrls != null) {
				urlList.addAll(unGiveOutUrls);
			}

			Date modifyTimeBefore = new Date(System.currentTimeMillis());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(modifyTimeBefore);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 2);
			modifyTimeBefore.setTime(calendar.getTimeInMillis());
			if (urlList.size() < count) {
				// 数据不够， 查询 下发超过2分钟，但未push结果的url
				List<FetchUrlPo> unPushUrls = fetchUrlDao.findFetchurl(task, FetchUrlPo.STATUS_PUSHED, modifyTimeBefore, count - urlList.size());
				LOG.debug("push down url again, count:{}", unGiveOutUrls.size());
				urlList.addAll(unPushUrls);
			}

			if (urlList.size() < count) {
				// 数据不够, 查询 失败的url
				List<FetchUrlPo> errorUrls = fetchUrlDao.findFetchurl(task, FetchUrlPo.STATUS_ERROR, modifyTimeBefore, count - urlList.size());
				LOG.debug("push down error url, count:{}", errorUrls.size());
				urlList.addAll(errorUrls);
			}

			if (urlList != null && !urlList.isEmpty()) {
				// 修改为已下发状态
				List<String> idList = new ArrayList<>();
				for (FetchUrlPo fetchUrlPo : urlList) {
					idList.add(fetchUrlPo.getId());
				}

				fetchUrlDao.markFetchUrlPushed(task, idList, new Time(System.currentTimeMillis()));
			}
		} finally {
		    lock.unlock();
        }

        LOG.debug("getGiveOutUrls cost {} ms", System.currentTimeMillis() - startTime);
		return urlList;
	}

    /**
     * 获取任务的下发fetchurl锁, 如果锁存在，返回以有的，不页面创建新的
     * @param task
     * @return
     */
    private Lock getGiveOutUrlsLock(SpiderTaskPo task) {
        Lock newLock = new ReentrantLock();
        Lock oldLock = GET_GIVEOUT_URLS_LOCK_MAP.putIfAbsent(task.getId(), newLock);
        return oldLock != null ? oldLock : newLock;
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

	@Override
	public int saveFetchSuccessInfo(SpiderTaskPo task, String urlId, Date time, int httpStatusCode) {
		return fetchUrlDao.updateFetchUrl(task, Arrays.asList(urlId), FetchUrlPo.STATUS_SUCCESS, time);
	}

}
