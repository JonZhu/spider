package com.zhujun.spider.master.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 读写锁工具
 * 
 * @author zhujun
 * @date 2016年7月13日
 *
 */
public class ReadWriteLockUtils {

	public final static Map<String, ReadWriteLock> LOCK_MAP = new HashMap<>();
	
	
	public static Lock getWriteLock(String lockKey) {
		ReadWriteLock lock = getLockObj(lockKey);
		return lock.writeLock();
	}
	
	
	public static Lock getReadLock(String lockKey) {
		ReadWriteLock lock = getLockObj(lockKey);
		return lock.readLock();
	}
	
	
	private static ReadWriteLock getLockObj(String lockKey) {
		synchronized (LOCK_MAP) {
			ReadWriteLock lock = LOCK_MAP.get(lockKey);
			if (lock == null) {
				lock = new ReentrantReadWriteLock(false);
				LOCK_MAP.put(lockKey, lock);
			}
			
			return lock;
		}
	}
	
}
