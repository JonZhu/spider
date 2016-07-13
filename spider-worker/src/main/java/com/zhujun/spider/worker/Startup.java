package com.zhujun.spider.worker;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.worker.fetch.FetchWorker;
import com.zhujun.spider.worker.mina.MinaClient;

/**
 * Worker 启动入口
 * 
 * @author zhujun
 * @date 2016年6月21日
 *
 */
public class Startup {

	private final static Logger LOG = LoggerFactory.getLogger(Startup.class);
	
	private final static AtomicInteger FETCHER_INDEX = new AtomicInteger(0);
	
	public static void main(String[] args) {
		
		if (args.length < 2) {
			LOG.error("Usage: com.zhujun.spider.worker.Startup masterHost port");
			System.exit(-1);
		}
		
		// 启动mina client 连接master
		MinaClient client = null;
		try {
			
			SocketAddress remoteAddress = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
			
			LOG.debug("开始启动Mina客户端");
			
			client = new MinaClient(remoteAddress);
			client.start();
			
			LOG.debug("Mina客户端启动完成");
		} catch (Exception e) {
			LOG.error("启动Mina客户端失败", e);
			System.exit(-1);
		}
		
		// 初始化从master拉取url线程
		startupPullUrlThread(client);
		
		// 启动fetch线程
		startupFetchThreads(client);
		
		
	}

	
	private static void startupPullUrlThread(MinaClient client) {
		PullUrlThread thread = new PullUrlThread(client);
		thread.setName("PullUrlThread");
		thread.start();
	}


	/**
	 * 启动fetch线程
	 * 
	 * @author zhujun
	 * @date 2016年6月22日
	 *
	 * @param client
	 * @param fetchQueue
	 */
	private static void startupFetchThreads(MinaClient client) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		
		ExecutorService fetchService = Executors.newFixedThreadPool(processorCount + 1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "fetcher-" + FETCHER_INDEX.getAndIncrement());
			}
		});
		
		for (int i = 0; i < processorCount + 1; i++) {
			FetchWorker fetcher = new FetchWorker(client);
			fetchService.execute(fetcher);
		}
		
	}
	
}
