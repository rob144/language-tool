package com.iparadigms.ipgrammar;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;
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
        RuleTestServlet testServlet = new RuleTestServlet();
        chTest.addServlet(new ServletHolder(testServlet), "/");
        
        ServletContextHandler chDict = new ServletContextHandler();
        chDict.setContextPath("/exportdictionary");
        DictionaryServlet dictExport = new DictionaryServlet();
        chDict.addServlet(new ServletHolder(dictExport), "/");
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { rh, chEnGb, chEnUs, chTest, chDict });
        
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