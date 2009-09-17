/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2009 John Lewis
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package net.sourceforge.cobertura.test.util

import static org.junit.Assert.*


public class WebappServer
{
	static final SIMPLE_SERVLET_CLASSNAME = "com.acme.servlet.SimpleServlet"
	
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
	def msecNeededToStop = 10000
	def coberturaAnt
	def modifyMainCoberturaDataFile

	/**
	 * Copies a web server installation into dir and deploys a webapp to it.
	 * 
	 */
	public deployApp(map)
	{
		modifyMainCoberturaDataFile = map.modifyMainCoberturaDataFile
		
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
		
		if (map.instrumentCobertura)
		{
			instrumentCoberturaJar(coberturaJar)
		}
		
		if (map.deployCoberturaFlush)
		{
			deployCoberturaFlush(map.instrumentCobertura)
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
	
	private deployCoberturaFlush(instrumentCobertura)
	{
		def webappsDir = new File(dir, "webapps")
		def war = new File(webappsDir, "coberturaFlush.war")
		
		/*
		 * Here we want to make the coberturaFlush war similar to the way
		 * the production build does it.  So, use the same file.
		 */
		GroovyScriptEngine gse = new GroovyScriptEngine(".")
		Binding binding = new Binding()
		binding.setVariable("ant", ant)
		gse.run("buildUtil.groovy", binding)
		//now call the buildWar closure
		binding.getVariable("buildWar")(war)
		
		
		if (instrumentCobertura)
		{
			instrumentWar(war, "net.sourceforge.cobertura.*")
		}
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
	
	private instrumentCoberturaJar(coberturaJar)
	{
		coberturaAnt.'cobertura-instrument'(datafile:"${dir}/cobertura.ser") {
			includeClasses(regex:"net.sourceforge.cobertura.*")
			excludeClasses(regex:'.*Test.*')
			fileset(dir:coberturaJar.getParent()) {
				include(name:coberturaJar.name)
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
			            xmlReport:new File("${dir}/coverage.xml"),
			            hostname:LOCALHOST,
			            webappPort:freePorts.webapp,
			    		coberturaAnt:coberturaAnt]
			
			data.datafile = getDatafileToUse()
			
			/*
			 * add a closure that can be called to flush the cobertura data
			 */
			data.flushCobertura = {
					def webappResponse = (new java.net.URL("http://${data.hostname}:${data.webappPort}/coberturaFlush/flushCobertura")).text
					assertEquals("Response of coberturaFlush unexpected", "", webappResponse.trim())
			}
		
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
	
	private getDatafileToUse()
	{
		def datafile = new File("${dir}/cobertura.ser")
		
		if (modifyMainCoberturaDataFile)
		{
			/*
			 * modify the cobertura.ser file created by the instrumentation target
			 * of the main build.xml file.   That way, the counts
			 * will end up in the overall coverage report
			 */
			def tempfile = new File("build/cobertura.ser")
			if (tempfile.exists())
			{
				datafile = tempfile
			}
			else
			{
				/*
				 * It looks like the main build.xml's instrument target has not been run.
				 * So, a coverage report is not being generated.
				 */
			}
		}
		return datafile
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
			if (modifyMainCoberturaDataFile)
			{
				sysproperty(key:'net.sourceforge.cobertura.datafile', value:getDatafileToUse().getAbsolutePath())
			}
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
