package net.sourceforge.cobertura.test.util



public class WebappServer
{
	static final SIMPLE_SERVLET_TEXT = '''
		package com.acme.servlet;
	
		import javax.servlet.http.HttpServlet;
		import javax.servlet.http.HttpServletRequest;
		import javax.servlet.http.HttpServletResponse;

		import java.io.PrintWriter;
		import java.io.IOException;
	
		public class SimpleServlet extends HttpServlet
		{
			protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException
			{
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				try
				{
					out.println("Hi");
				}
				finally
				{
					if (out != null)
					{
						out.close();
					}
				}
			}
		}
		'''
		
	static final SIMPLE_SERVLET_WEB_XML_TEXT = '''<?xml version="1.0" encoding="UTF-8"?>
<web-app version="2.4" xmlns="http://java.sun.com/xml/ns/j2ee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd">
		<servlet>
			<servlet-name>SimpleServlet</servlet-name>
			<servlet-class>com.acme.servlet.SimpleServlet</servlet-class>
	    </servlet>
	    <servlet-mapping>
	    	<servlet-name>SimpleServlet</servlet-name>
	    	<url-pattern>/SimpleServlet</url-pattern>
	    </servlet-mapping>
</web-app>
'''

	private static final ant = TestUtil.antBuilder
	private static final LOCALHOST = "127.0.0.1"
	
	def dir = "."
	def msecNeededToStop = 1000
	def coberturaAnt

	/**
	 * Copies a web server installation into dir and deploys a webapp to it.
	 * 
	 */
	public deployApp(map)
	{
		def extractedDir = new File(dir, "extracted")
		def webInfDir = new File(extractedDir, "WEB-INF")
		def classesDir = new File(webInfDir, "classes")
		
		def webInfFile = writeWebInfFile(webInfDir, map.webInfText)
		
		compileSourceFiles(map.srcDir, classesDir)
		
		copyJettyFiles(dir)
		
		def war = makeWarFile(map.appName, webInfFile, classesDir)
		
		ant.delete(dir:extractedDir)
		
		def coberturaJar = createCoberturaJar()

		coberturaAnt = TestUtil.getCoberturaAntBuilder(TestUtil.getCoberturaClassDir())
		
		if (map.instrumentRegEx)
		{
			instrumentWar(war, map.instrumentRegEx)
		}
	}
	
	private writeWebInfFile(webInfDir, text)
	{
		webInfDir.mkdirs()
		def webInfFile = new File(webInfDir, "web.xml")
		
		webInfFile.write(text)
		return webInfFile
	}
	
	private compileSourceFiles(srcDir, classesDir)
	{
		ant.mkdir(dir:classesDir)
		ant.javac(srcdir:srcDir, destdir:classesDir, debug:'true') {
			classpath {
				fileset(dir:"jetty") {
					include(name:"**/*.jar")
				}
			}
		}
	}
	
	private copyJettyFiles(todir)
	{
		ant.copy(todir:todir) {
			fileset(dir:"jetty")
		}
	}
	
	private makeWarFile(appName, webInfFile, classesDir)
	{
		def webappsDir = new File(dir, "webapps")
		def war = new File(webappsDir, "${appName}.war")
		
		ant.war(destfile:war, webxml:webInfFile) {
			classes(dir:classesDir)
		}
		return war
	}
	
	private instrumentWar(war, instrumentRegEx)
	{
		coberturaAnt.'cobertura-instrument'(datafile:"${dir}/cobertura.ser") {
			includeClasses(regex:instrumentRegEx)
			excludeClasses(regex:'.*Test.*')
			fileset(dir:war.getParent()) {
				include(name:'*.war')
			}
		}
	}
	
	/**
	 * Starts the web server and calls the closure.   The web server will be stopped 
	 * when the closure exits (successfully or not).
	 * 
	 */
	public withRunningServer(closure)
	{
		def freePorts = findFreePorts()
				
		startWebServer(freePorts)
		def data = null
		try
		{
			data = [
			            datafile:new File("${dir}/cobertura.ser"), 
			            xmlReport:new File("${dir}/coverage.xml"),
			            hostname:LOCALHOST,
			            webappPort:freePorts.webapp,
			    		coberturaAnt:coberturaAnt]
		
			TestUtil.waitForLiveServer(data.hostname, data.webappPort, 1)  //1 min timeout
			closure.call(data)
		}
		finally
		{
			stopWebServer(freePorts.stop)
		}
		
		/*
		 * Wait a bit for the server to stop completely
		 */
		Thread.sleep(msecNeededToStop)
		coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
		def dom = TestUtil.getXMLReportDOM(data.xmlReport)
		data.dom = dom
		return data
	}
	
	/**
	 * Find two port numbers that can be used to start a webapp server (one port for
	 * http connections; one port for a stop command).
	 */
	private findFreePorts()
	{
		def freePorts = [webapp:null, stop:null]
		
		def webappSocket = null
		def stopSocket = null
		
		try
		{
			webappSocket = new ServerSocket(0, 1, null)
			stopSocket = new ServerSocket(0, 1, null)
			
			freePorts.webapp = webappSocket.getLocalPort()
		
			freePorts.stop = stopSocket.getLocalPort()
		}
		finally 
		{
			closeSocket(webappSocket)
			closeSocket(stopSocket)
		}
		return freePorts
	}
	
	private closeSocket(socket)
	{
		try
		{
			if (socket) {
				socket.close()
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err)
		}
	}
	
	private startWebServer(freePorts)
	{
		ant.echo(message:"Starting Jetty webapp server on ${freePorts.webapp}")
		ant.java(jar:"${dir}/start.jar", dir:dir, fork:true, spawn:true) {
			sysproperty(key:'jetty.port', value:freePorts.webapp)
			sysproperty(key:'STOP.PORT', value:freePorts.stop)
			sysproperty(key:'STOP.KEY', value:'cobertura')
		}
	}
	
	private stopWebServer(stopPort)
	{
		ant.java(jar:"${dir}/start.jar", dir:dir, fork:true) {
			arg(value:'--stop')
			sysproperty(key:'STOP.PORT', value:stopPort)
			sysproperty(key:'STOP.KEY', value:'cobertura')
		}
	}
	
	private createCoberturaJar()
	{
		def coberturaJar = new File(dir, "lib/cobertura.jar")
		def coberturaClassDir = TestUtil.getCoberturaClassDir()
		ant.zip(destfile:coberturaJar) {
			fileset(dir:coberturaClassDir)
		}
		return coberturaJar
	}
	
	public static writeSimpleServletSource(srcDir)
	{
		def servletSourceFile = new File(srcDir, "com/acme/servlet/SimpleServlet.java")
		servletSourceFile.parentFile.mkdirs()
		
		servletSourceFile.write(SIMPLE_SERVLET_TEXT)
	}
}
