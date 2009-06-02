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

import junit.framework.Assert

public class TestUtil {

	public static final antBuilder = new AntBuilder()
	
	private static File coberturaClassDir

	public static final String SOURCE_TEXT = '''
		package a.mypackage;

		public class SimpleSource {
			public void aSimpleMethod() {
			}
		}
		'''

	public static final String SIMPLE_SOURCE_PATHNAME = 'a/mypackage/SimpleSource.java'

	/**
	 * Usage TestUtil.withTempDir { tempDir -> doSomethingWith(tempDir) }
	 * 
	 * Create a directory under the system's temporary directory, and automatically
	 * delete it before returning from withTempDir.
	 * 
	 */
	public static withTempDir(Closure worker) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File tempSubdir = new File(tempDir, "cobertura_test" + System.currentTimeMillis());
        Throwable savedThrowable = null
		try {
			tempSubdir.mkdirs()
			//now call the closure passing it the subdir
			worker(tempSubdir)
		} catch (Throwable t) {
			savedThrowable = t
		} finally {
			try {
				antBuilder.delete(dir:tempSubdir, failonerror:false)
			} catch (Throwable t) {
				if (savedThrowable) {
					//something went wrong with the delete, but the savedThrowable is more important
					t.printStackTrace(System.err)
					throw savedThrowable
				} else {
					throw t
				}
			}
			if (savedThrowable) {
				throw savedThrowable
			}
		}
	}

	public static createSourceArchive(dir)
	{
		/*
		 * Create a simple source file in the temporary directory
		 */
		def sourceDir = new File(dir, "src")
		def sourceFile = new File(sourceDir, SIMPLE_SOURCE_PATHNAME)
		sourceFile.parentFile.mkdirs()
		sourceFile.write(SOURCE_TEXT)

		//create a source zip file with the simple source file
		def zipDir = new File(dir, "zip")
		zipDir.mkdirs()
		def zipFile = new File(zipDir, "source.zip")			
		antBuilder.zip(destfile:zipFile, basedir:sourceDir)
		
		//now delete the source file to make sure we use the zip file
		Assert.assertTrue(sourceFile.delete())

		return zipFile
	}
	
	public static synchronized getCoberturaClassDir() {
		if (coberturaClassDir == null)
		{
			coberturaClassDir = new File("build/test/cobertura_classes")
			coberturaClassDir.mkdirs()
			antBuilder.javac(srcdir:'src', destdir:coberturaClassDir, debug:true) {
				classpath {
					fileset(dir:'lib') {
						include(name:'**/*.jar')
					}
				}
			}
		}
		return coberturaClassDir
	}
	
	private static waitForLiveServer(webContainerHostname, webContainerPort, timeoutMin) {
		InetSocketAddress address = new InetSocketAddress(webContainerHostname, webContainerPort);
		
		antBuilder.echo(message:"Waiting $timeoutMin min for web server...")
		long beginTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis() + (timeoutMin * 60 * 1000); 
		boolean portOpened = false;
		while ((!portOpened) && (System.currentTimeMillis() < endTime)) {
			portOpened = trySocket(address);
			
			if (portOpened) {
				antBuilder.echo("Web server has opened the port in " + (System.currentTimeMillis() - beginTime)/1000.0/60.0 + " min.");
			} else {
				try {
					Thread.sleep(2000);  //2 sec
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (!portOpened) {
			throw new RuntimeException("Timed out waiting for webapp server to initialize");
		}
	}
	
	/**
	 * Checks to see if a socket is opened.
	 */
	public static boolean trySocket(InetSocketAddress address) {
		boolean success = false;
		
		Socket socket = null
		try
		{
			socket = new Socket()
			socket.connect(address);
			success = true;
		}
		catch (ConnectException e) {
			//this is expected
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		finally {
			if (socket)
			{
				socket.close();
			}
		}
		return success;
	}

	public static getXMLReportDOM(xmlReport)
	{
		def parser = new XmlParser()
		parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false)
		parser.parse(xmlReport)
	}
	
	public static isMethodHit(dom, methodName)
	{
		def methods = dom.packages.'package'.classes.'class'.methods.method
		def getMethod = methods.grep { it.'@name' == methodName }
		def hitsPerLine = getMethod.lines.line.'@hits'[0]
		return (hitsPerLine.any { it.toInteger() >= 1 })
	}
	
	public static getHitCount(dom, className, methodName)
	{
		def classes = dom.packages.'package'.classes.'class'
		def clazz = classes.grep { it.'@name' == className }[0]
		if (clazz == null)
		{
			return 0
		}
		def methods = clazz.methods.method
		def method = methods.grep { it.'@name' == methodName }[0]
		if (method == null)
		{
			return 0
		}
		def firstLine = method.lines.line[0]
		if (firstLine == null)
		{
			return 0
		}
		def hitCount = firstLine.'@hits'
		return hitCount.toInteger()
	}
	
	public static getCoberturaAntBuilder(cobertura)
	{
		def ret = new AntBuilder()
		ret.taskdef(resource:"tasks.properties") {
			classpath {
				pathelement(location:cobertura)
				fileset(dir:'lib') {
					include(name:"**/*.jar")
				}
			}
		}
		ret.taskdef(name:'groovyc', classname:'org.codehaus.groovy.ant.Groovyc') {
			classpath {
				fileset(dir:'antLibrary/common/groovy') {
					include(name:'*.groovy')
				}
			}
		}
		return ret
	}
}
