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

public class SwitchFunctionalTest {
	def ant = TestUtil.getCoberturaAntBuilder(TestUtil.getCoberturaClassDir())
	def testUtil = new TestUtil()

	@Test
	void noDefaultTest() {
		/*
		 * Use a temporary directory and create a Main.java source file that
		 * has a switch statement with no default.
		 */
		TestUtil.withTempDir { tempDir ->
			def srcDir = new File(tempDir, "src")
			def instrumentDir = new File(tempDir, "instrument")
			
			def mainSourceFile = new File(srcDir, "mypackage/Main.java")
			def datafile = new File(srcDir, "cobertura.ser")
			mainSourceFile.parentFile.mkdirs()
			
			mainSourceFile.write """
package mypackage;

public class Main {
	public static void main(String[] args) {
		Main main = new Main();
		main.callNoDefaultSwitch();
		main.callSwitchFallThrough();
	}
	
	public void callNoDefaultSwitch() {
        int i=2;
        switch (i) {
         case 1:
             System.out.println("1");
             break;
         case 2:
             System.out.println("2");
             break;
        }
	}

    public void callSwitchFallThrough() {
        switchFallThrough(5);
        switchFallThrough(1);
    }

    private void switchFallThrough(int i) {
        switch (i) {
         case 2:
             System.out.println("2");
          break;
         case 3:
          break;
         case 1: // fall-through!
         default:
             System.out.println("1 or default");
        }
    }
}
			"""

			testUtil.compileSource(ant, srcDir)
			
			testUtil.instrumentClasses(ant, srcDir, datafile, instrumentDir)
			
			/*
			 * Kick off the Main (instrumented) class.
			 */
			ant.java(classname:'mypackage.Main', dir:srcDir, fork:true, failonerror:true) {
				classpath {
					dirset(dir:instrumentDir)
					dirset(dir:testUtil.coberturaClassDir)
				}
			}

			/*
			 * Now create a cobertura xml file and make sure the correct counts are in it.
			 */
			ant.'cobertura-report'(datafile:datafile, format:'xml', destdir:srcDir)
			def dom = TestUtil.getXMLReportDOM("${srcDir}/coverage.xml")
				
			def lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'callNoDefaultSwitch')
			
			def noDefaultSwitchLine = lines.grep {it.number == '13'}[0]
			assertEquals('33% (1/3)', noDefaultSwitchLine.conditionCoverage)

			lines = TestUtil.getLineCounts(dom, 'mypackage.Main', 'switchFallThrough')
			
			def fallThroughSwitchLine = lines.grep {it.number == '29'}[0]
   			assertEquals('33% (1/3)', fallThroughSwitchLine.conditionCoverage)
		}
	}

}