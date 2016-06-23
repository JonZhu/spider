package com.zhujun.spider.master.ui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.zhujun.spider.master.server.IServer;
import com.zhujun.spider.master.ui.servlet.IndexServlet;

/**
 * 用户界面接口 服务
 * 
 * @author zhujun
 * @date 2016年6月23日
 *
 */
public class UIServer implements IServer {

	private int port;
	private Server jettyServer;
	
	public UIServer(int port) {
		this.port = port;
	}
	
	@Override
	public void start() {
		jettyServer = new Server(port);
		
		ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
		
		// resource ./ui -> /
		ContextHandler resourceContextHandler = new ContextHandler("/");
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setResourceBase("./ui");
		resourceHandler.setWelcomeFiles(new String[]{"index.html"});
		resourceContextHandler.setHandler(resourceHandler);
		handlerCollection.addHandler(resourceContextHandler);
		
		// servlet
		ServletContextHandler servletContextHandler = new ServletContextHandler();
		servletContextHandler.addServlet(IndexServlet.class, "*.do");
		handlerCollection.addHandler(servletContextHandler);
		
		jettyServer.setHandler(handlerCollection);
		try {
			jettyServer.start();
		} catch (Exception e) {
			throw new RuntimeException("启动UIServer失败", e);
		}
	}

	@Override
	public void stop() {
		if (jettyServer != null) {
			try {
				jettyServer.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	
	
}
