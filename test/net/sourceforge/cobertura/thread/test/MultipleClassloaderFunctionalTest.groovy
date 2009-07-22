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

package net.sourceforge.cobertura.thread.test

import net.sourceforge.cobertura.test.util.TestUtil

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

public class MultipleClassloaderFunctionalTest {
	
	/*
	 * This tests the case where there are multiple classloaders that each
	 * load the Cobertura classes.   Cobertura uses static fields to
	 * hold the data.   When there are multiple classloaders, each classloader
	 * will keep track of the line counts for the classes that it loads.  
	 * 
	 * The static initializers for the Cobertura classes are also called for
	 * each classloader.   So, there is one shutdown hook for each classloader.
	 * So, when the JVM exits, each shutdown hook will try to write the
	 * data it has kept to the datafile.   They will do this at the same
	 * time.   Before Java 6, this seemed to work fine, but with Java 6, there
	 * seems to have been a change with how file locks are implemented.   So,
	 * care has to be taken to make sure only one thread locks a file at a time.
	 * 
	 */

	def ant = TestUtil.getCoberturaAntBuilder(TestUtil.getCoberturaClassDir())

	private static CALLED_CODE = """
package mypackage;

public class Called {
	public static void callThis()
	{
	}
}
"""

	@Before
	void setUp() {
	}
	
	@Test
	void multipleClassloadersTest() {
		ant.echo(message:"Running multiple classloader test.")
		runTest()
	}
	
	/*
	 * This code creates a Java class that has a main method that will create two
	 * classloaders.   Each classloader will then load the mypackage.Called class
	 * defined above.  Then, the mypackage.Called.callThis() method is called once.
	 */
	def getMainCode(instrumentDir) {
		return """
package mypackage;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.lang.reflect.Method;

public class Main {
	public static void main(String[] args) throws Throwable
	{
		createClassloaderAndCallMethod();
		createClassloaderAndCallMethod();
	}

	/*
	 * Create a classloader that loads the instrumented code and the cobertura classes.
	 * Then, call the mypackage.Called.callThis() static method.
	 */
	public static void createClassloaderAndCallMethod() throws Throwable
	{
		File instrumentDir = new File("${instrumentDir.absolutePath.replace('\\', '\\\\')}");
		File coberturaClassDir = new File("${TestUtil.getCoberturaClassDir().absolutePath.replace('\\', '\\\\')}");

		/*
		 * Create a classloader with a null parent classloader to ensure that this
		 * classloader loads the Cobertura classes itself.
		 */
        URLClassLoader loader = new URLClassLoader(
        		new URL[] {instrumentDir.toURL(), coberturaClassDir.toURL()}, null);

		// use reflection to call mypackage.Called.callThis() once.
        Class calledClass = loader.loadClass("mypackage.Called");
        Method method = calledClass.getMethod("callThis", null);

        method.invoke(null, null);
	}
}
"""
	}

	private void runTest()
	{
		/*
		 * Use a temporary directory and create a Main.java source file
		 * that creates multiple classloaders.   Also, create a Called.java
		 * file that defines the mypackage.Called.callThis() method that
		 * will be called by mypackage.Main.main().
		 */
		TestUtil.withTempDir { tempDir ->
			def srcDir = new File(tempDir, "src")
			def instrumentDir = new File(tempDir, "instrument")
			
			def mainSourceFile = new File(srcDir, "mypackage/Main.java")
			def datafile = new File(srcDir, "cobertura.ser")
			mainSourceFile.parentFile.mkdirs()
			
			mainSourceFile.write(getMainCode(instrumentDir))
			
			def calledSourceFile = new File(srcDir, "mypackage/Called.java")
			calledSourceFile.write(CALLED_CODE)
			
			compileSource(srcDir)
			
			instrumentClasses(srcDir, datafile, instrumentDir)
			
			/*
			 * Kick off the Main class.   I'll use the non-instrumented classes, but
			 * I think you could use the instrumented ones as well.
			 */
			ant.java(classname:'mypackage.Main', dir:srcDir, fork:true, failonerror:true) {
				classpath {
					dirset(dir:srcDir)
				}
			}

			/*
			 * Now create a cobertura xml file and make sure the correct counts are in it.
			 */
			ant.'cobertura-report'(datafile:datafile, format:'xml', destdir:srcDir)
			def dom = TestUtil.getXMLReportDOM("${srcDir}/coverage.xml")
				
			def lines = TestUtil.getLineCounts(dom, 'mypackage.Called', 'callThis')
			
			// the callThis() method is empty, so the only line is the ending brace.
			assertEquals(1, lines.size())
			lines.each {
				// The callThis() method is called once for each classloader.
				// There are 2 classloaders.
				assertEquals("hit count incorrect", 2, it.hits)
			}	
		}
	}
	
	def compileSource(srcDir)
	{
		ant.groovyc(srcdir:srcDir, destDir:srcDir) {
			javac(debug:'true')
		}
	}
	
	def instrumentClasses(srcDir, datafile, todir)
	{
		ant.'cobertura-instrument'(datafile:datafile, todir:todir) {
			includeClasses(regex:'mypackage.*')
			fileset(dir:srcDir) {
				include(name:'**/*.class')
			}
		}
	}
}
