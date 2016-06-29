package com.zhujun.spider.master.ui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

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
		
		WebAppContext webApp = new WebAppContext();
		webApp = new WebAppContext();
		webApp.setContextPath("/");
		webApp.setResourceBase("./ui"); // resource
		webApp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
		webApp.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
		
		// servlet
		webApp.addServlet(IndexServlet.class, "*.do");
		
		jettyServer.setHandler(webApp);
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
