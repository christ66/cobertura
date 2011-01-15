/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2010 John Lewis
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

package net.sourceforge.cobertura.test

import net.sourceforge.cobertura.test.util.TestUtil

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

public class FunctionalTest {
	def ant = TestUtil.getCoberturaAntBuilder(TestUtil.getCoberturaClassDir())
	def testUtil = new TestUtil()
	def dom
	def ignoreUtil

// Interfaces are not instrumented (yet).
//	@Test
//	void interfaceFunctionalTest() {
//		/*
//		 * Use a temporary directory and create a few sources files.
//		 */
//		TestUtil.withTempDir { tempDir ->
//			def srcDir = new File(tempDir, "src")
//			def reportDir = new File(tempDir, "report")
//			def instrumentDir = new File(tempDir, "instrument")
//			
//			def mainSourceFile = new File(srcDir, "mypackage/Main.java")
//			def datafile = new File(srcDir, "cobertura.ser")
//			mainSourceFile.parentFile.mkdirs()
//			
//			def interfaceSourceFile = new File(srcDir, "mypackage/MyInterface.java")
//			
//			interfaceSourceFile.write """
//package mypackage;
//
//public interface MyInterface {
//	public static final Object MY_CONSTANT = new Object();  /* the test expects this to be line 5 */
//}
//"""
//			
//			mainSourceFile.write """
//package mypackage;
//
//public class Main implements MyInterface {
//
//    public static void main(String[] args) {
//			System.out.println(new Main());
//			System.out.println(MY_CONSTANT);
//    }
//}
//"""
//			testUtil.compileSource(ant, srcDir)		
//			testUtil.instrumentClasses(ant, srcDir, datafile, instrumentDir)			
//			/*
//			 * Kick off the Main (instrumented) class.
//			 */
//			ant.java(classname:'mypackage.Main', dir:srcDir, fork:true, failonerror:true) {
//				classpath {
//					dirset(dir:instrumentDir)
//					dirset(dir:srcDir)
//					dirset(dir:testUtil.coberturaClassDir)
//				}
//			}
//
//			/*
//			* Now create a cobertura html report and make sure the files are created.
//			*/
//			ant.'cobertura-report'(datafile:datafile, format:'html', destdir:reportDir, srcdir:srcDir)
//			assertTrue(new File(reportDir, "index.html").exists())
//			assertTrue(new File(reportDir, "mypackage.Main.html").exists())
//			assertTrue(new File(reportDir, "mypackage.MyInterface.html").exists())
//
//			/*
//			* Now create a cobertura xml file and make sure the correct counts are in it.
//			*/
//			ant.'cobertura-report'(datafile:datafile, format:'xml', destdir:srcDir)
//			def dom = TestUtil.getXMLReportDOM("${srcDir}/coverage.xml")
//		   
//			def lines = TestUtil.getLineCounts(dom, 'mypackage.MyInterface', '<clinit>')
//			
//			def myconstantLine = lines.grep {it.number == '5'}[0]
//			assertEquals(1, myconstantLine.hits)
//
//		}
//	}

	@Test
	void conditionalInFinallyFunctionalTest() {
		/*
		 * Use a temporary directory and create a few sources files.
		 */
		TestUtil.withTempDir { tempDir ->
			def srcDir = new File(tempDir, "src")
			def reportDir = new File(tempDir, "report")
			def instrumentDir = new File(tempDir, "instrument")
			
			def mainSourceFile = new File(srcDir, "mypackage/Main.java")
			def datafile = new File(srcDir, "cobertura.ser")
			mainSourceFile.parentFile.mkdirs()
			
			mainSourceFile.write """
package mypackage;



public class Main {

	private boolean isDisabled() {
		return true;
	}

	private void doSomething() {
	}

    public void aMethod() {
		boolean disabled = false;
		try {
			disabled = isDisabled();
		} finally {
			if (disabled)
				doSomething();
		}
    }


}
"""

			testUtil.compileSource(ant, srcDir)
			
			testUtil.instrumentClasses(ant, srcDir, datafile, instrumentDir)
			
			/*
			* Now create a cobertura xml file and make sure the correct counts are in it.
			*/
			ant.'cobertura-report'(datafile:datafile, format:'xml', destdir:srcDir)
			def dom = TestUtil.getXMLReportDOM("${srcDir}/coverage.xml")
		   
			def lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'aMethod')
			
			def conditionalLine = lines.grep {it.number == '20'}[0]
			assertEquals('0% (0/2)', conditionalLine.conditionCoverage)

		}
	}

	@Test
	void callJunit() {
		/*
		 * Use a temporary directory and create a few sources files.
		 */
		TestUtil.withTempDir { tempDir ->
			def srcDir = new File(tempDir, "src")
			
			def reportDir = new File(tempDir, "report")
			reportDir.mkdirs()
			
			def instrumentDir = new File(tempDir, "instrument")
			instrumentDir.mkdirs()
			
			def buildDir = new File(tempDir, "build")
			buildDir.mkdirs()
			
			def testSourceFile = new File(srcDir, "mypackage/MyTest.groovy")
			testSourceFile.parentFile.mkdirs()
			testSourceFile.write '''
package mypackage

import junit.framework.TestSuite
import junit.framework.Test


public class MyTest extends TestSuite {
	public MyTest(String arg0) {
		super(arg0);
	}

	public static Test suite() {
		
		// do something that will cause Sub's static initializer to run
		Sub.class
		
		return new TestSuite("Empty Suite")
	}
}



'''
			
			def superSourceFile = new File(srcDir, "mypackage/Super.java")
			def subSourceFile = new File(srcDir, "mypackage/Sub.java")
			def datafile = new File(srcDir, "cobertura.ser")
			
			superSourceFile.write """
package mypackage;

public class Super {
	static {
		Sub.aStaticMethod();
	};
}
"""
			
			subSourceFile.write """
package mypackage;

public class Sub extends Super {

	public static void aStaticMethod() {
		System.out.println("aStaticMethod called");
	}

}
"""

			// compile to the srcDir
			testUtil.compileSource(ant, srcDir)

			// instrument all but the test class (in place)
			testUtil.instrumentClasses(ant, srcDir, datafile, instrumentDir,
				[excludeClassesRegexList:['mypackage.MyTest']])
			
			// run the MyTest
			testUtil.junit(
				testClass     : 'mypackage.MyTest',
				ant           : ant,
				buildDir      : srcDir,
				instrumentDir : instrumentDir,
				reportDir     : reportDir,
			)
			
			/*
			* Now create a cobertura xml file and make sure the correct counts are in it.
			*/
		   ant.'cobertura-report'(datafile:datafile, format:'xml', destdir:srcDir)
		   def dom = TestUtil.getXMLReportDOM("${srcDir}/coverage.xml")
			   
		   def lines = TestUtil.getLineCounts(dom, 'mypackage.Sub', 'aStaticMethod')
		   
		   def aStaticMethodLine = lines.grep {it.number == '7'}[0]
		   assertEquals(1, aStaticMethodLine.hits)

		}
	}

	@Test
	void simpleFunctionalTest() {
		/*
		 * Use a temporary directory and create a few sources files.
		 */
		TestUtil.withTempDir { tempDir ->
			def srcDir = new File(tempDir, "src")
			def reportDir = new File(tempDir, "report")
			def instrumentDir = new File(tempDir, "instrument")
			
			def mainSourceFile = new File(srcDir, "mypackage/Main.java")
			def simpleSourceFile = new File(srcDir, "mypackage/Simple.java")
			def datafile = new File(srcDir, "cobertura.ser")
			mainSourceFile.parentFile.mkdirs()
			
			simpleSourceFile.write """
package mypackage;

public class Simple {
}
"""
			
			mainSourceFile.write """
package mypackage;



public class Main {


    public boolean isSimple() {
        return false;
    }


    private Object create() {
        if (isSimple()) {
            Object result = new Simple();
        } else {
            Object result = new Main();
        }
        return null;

    }


}
			"""

			testUtil.compileSource(ant, srcDir)
			
			testUtil.instrumentClasses(ant, srcDir, datafile, instrumentDir)
			
		}
	}
}
