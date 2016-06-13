package com.zhujun.spider.master.berkeleydb;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Sequence;
import com.sleepycat.je.SequenceConfig;
import com.sleepycat.je.Transaction;

public class BerkeleyDBTest {

	Environment env = null;
	Database db = null;
	
	
	@Test
	public void testWrite() {
		
		openDatabase();
		
		DatabaseEntry key = new DatabaseEntry("spider.fetch.url".getBytes());
		Transaction transaction = env.beginTransaction(null, null);
		for (int i = 0; i < 1000000; i++) {
			OperationStatus status = db.put(transaction, key, new DatabaseEntry(("http://test.com/" + i).getBytes()));
			if (i % 1000 == 999) {
				transaction.commit();
				transaction = env.beginTransaction(null, null);
			}
			System.out.println(i);
		}
		
		transaction.commit();
		System.out.println(db.count());
		
		IOUtils.closeQuietly(db);
	}

	private void openDatabase() {
		File envHome = new File("E:/tmp/berkeleydb");
		if (!envHome.exists()) {
			envHome.mkdirs();
		}
		
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		envConfig.setReadOnly(false);
		envConfig.setTransactional(true);
		env = new Environment(envHome, envConfig);
		
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setSortedDuplicates(true);
		dbConfig.setReadOnly(false);
		dbConfig.setTransactional(true);
		db = env.openDatabase(null, "spider", dbConfig);
		
	}
	
	@Test
	public void testRead() {
		openDatabase();
		System.out.println(db.count());
		
		CursorConfig cursorConfig = new CursorConfig();
		Transaction transaction = env.beginTransaction(null, null);
		Cursor cursor = db.openCursor(transaction, cursorConfig);
		
		DatabaseEntry key = new DatabaseEntry("spider.fetch.url".getBytes());
		DatabaseEntry value = new DatabaseEntry();
		
		OperationStatus status = cursor.getFirst(key, value, LockMode.DEFAULT);
		while (status == OperationStatus.SUCCESS) {
			System.out.println(new String(value.getData()));
			cursor.delete();
			status = cursor.getNext(key, value, LockMode.DEFAULT);
		}
		
		cursor.close();
		transaction.commit();
		System.out.println(db.count());
	}

}
