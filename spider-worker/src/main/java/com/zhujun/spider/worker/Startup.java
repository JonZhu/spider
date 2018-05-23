package com.zhujun.spider.worker;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.zhujun.spider.worker.mina.MinaPassiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.worker.fetch.FetchWorker;
import com.zhujun.spider.worker.mina.MinaInitiativeClient;

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

	private final static String USAGE = "Usage: \n" +
			"1.主动连接master：com.zhujun.spider.worker.Startup masterHost port\n" +
			"2.被动连接master：com.zhujun.spider.worker.Startup listenPort";
	
	public static void main(String[] args) {
		if (args == null || args.length < 1 || args.length > 2) {
			System.out.println(USAGE);
			System.exit(-1);
		}

		// 启动mina client 连接master
		MasterClient client = null;
		if (args.length == 2) {
			String masterHost = args[0];
			Integer masterPort = Integer.valueOf(args[1]);
			LOG.info("主动连接方法启动, master: {}:{}", masterHost, masterPort);
			InetSocketAddress remoteAddress = new InetSocketAddress(masterHost, masterPort);
			client = new MinaInitiativeClient(remoteAddress);
		} else {
			// args.length == 1
			Integer listenPort = Integer.valueOf(args[0]);
			client = new MinaPassiveClient(listenPort);
			LOG.info("被动连接方法启动, listenPort: {}", listenPort);
		}
		client.init();
		client.connectMaster();
		LOG.debug("mina client connected to master");
		
		// 初始化从master拉取url线程
		startupPullUrlThread(client);
		
		// 启动fetch线程
		startupFetchThreads(client);

	}

	
	private static void startupPullUrlThread(MasterClient client) {
		PullUrlThread thread = new PullUrlThread(client);
		thread.setName("PullUrlThread");
		thread.start();
		LOG.info("启动PullUrlThread");
	}


	/**
	 * 启动fetch线程
	 * 
	 * @author zhujun
	 * @date 2016年6月22日
	 *
	 * @param client
	 */
	private static void startupFetchThreads(MasterClient client) {
		LOG.info("开始 初始化fetcher线程池");
		int processorCount = Runtime.getRuntime().availableProcessors();
		int min = 16;
		int fetcherCount = Math.max(min, processorCount * 2 + 1);
		
		ExecutorService fetchService = Executors.newFixedThreadPool(fetcherCount, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "fetcher-" + FETCHER_INDEX.getAndIncrement());
			}
		});
		
		for (int i = 0; i < fetcherCount; i++) {
			FetchWorker fetcher = new FetchWorker(client);
			fetchService.execute(fetcher);
		}

		LOG.info("结束 初始化fetcher线程池, fetcherCount: {}", fetcherCount);
	}
	
}
