package com.iparadigms.ipgrammar;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ServerManager {
	
	private Server server;
	
	public ServerManager() throws Exception {
        server = new Server(6819);
        
        ResourceHandler rh = new ResourceHandler();
        rh.setResourceBase("bin/webapp");
        rh.setDirectoriesListed(true);
        rh.setWelcomeFiles(new String[]{ "index.html" });
        // The resource base indicates where the files should be served out of.
        
        ServletContextHandler chEnGb = new ServletContextHandler();
        chEnGb.setContextPath("/en-GB");
        TextEngineServlet enGBServlet = new TextEngineServlet ("en-GB");
        chEnGb.addServlet(new ServletHolder(enGBServlet), "/checktext");
        chEnGb.addServlet(new ServletHolder(enGBServlet), "/testrules");
        
        ServletContextHandler chEnUs = new ServletContextHandler();
        chEnUs.setContextPath("/en-US");
        chEnUs.addServlet(new ServletHolder(new TextEngineServlet("en-US")), "/checktext");
        
        ServletContextHandler chTest = new ServletContextHandler();
        chTest.setContextPath("/test");
        chTest.addServlet(new ServletHolder(new RuleTestServlet()), "/");

        ServletContextHandler chDict = new ServletContextHandler();
        chDict.setContextPath("/dictionary");
        chDict.addServlet(new ServletHolder(new DictionaryServlet()), "/");
        
        ServletContextHandler chUpdate = new ServletContextHandler();
        chUpdate.setContextPath("/update");
        chUpdate.addServlet(new ServletHolder(new UpdateServlet()), "/");
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { rh, chEnGb, chEnUs, chTest, chDict, chUpdate });
        
        server.setHandler(handlers);
	}
	
	public void start() throws Exception {
		server.start();
		server.join();
	}
	
	public void stop() throws Exception {
		server.stop();
	}
	
	protected void finalize () throws Throwable {
		stop();
	}
}