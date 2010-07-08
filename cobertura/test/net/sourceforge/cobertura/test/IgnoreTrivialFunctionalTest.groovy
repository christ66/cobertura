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

public class IgnoreTrivialFunctionalTest {
	def ant = TestUtil.getCoberturaAntBuilder(TestUtil.getCoberturaClassDir())
	def testUtil = new TestUtil()
	def dom

	@Test
	void ignoreTrivialTest() {
		/*
		 * Use a temporary directory and create a Main.java source file that
		 * has trivial methods such as .
		 */
		TestUtil.withTempDir { tempDir ->
			def srcDir = new File(tempDir, "src")
			def instrumentDir = new File(tempDir, "instrument")
			
			def mainSourceFile = new File(srcDir, "mypackage/Main.java")
			def datafile = new File(srcDir, "cobertura.ser")
			mainSourceFile.parentFile.mkdirs()
			
			mainSourceFile.write """
package mypackage;

public class Main extends Thread {
			
	public static interface MyInterface {
		public void myInterfaceMethod();
	}
	public static class MyObject implements MyInterface
	{
		public void myInterfaceMethod()
		{
		}
	}

	public static void main(String[] args) {
		Main main = new Main();
			
		/*
		 * Call all methods so they will be considered "covered" unless
		 * they are ignored as trivial.
		 *
		 * These are in no particular order.
		 */
		main.getterTrivial();
		main.empty();
		main.getVoid();
		main.getIntWithIntParm(0);
		main.isBool();
		main.hasBool();
		main.set();
		main.setInt(1);
		main.setIntWithTwoParms(1, 2);
		main.getMultiDimArray();
		main.setIncrement(1);
		main.setConst("");
		main.getArray();
		main.getObject();
		main.getStatic();
		main.setStatic(1);
		main.setISTORE(1);
		main.setLSTORE(1);
		main.setFSTORE((float)1.0);
		main.setDSTORE(1.0);
		main.setASTORE(null);
		main.getINVOKEVIRTUAL();
		main.getINVOKESPECIAL();
		main.getINVOKESTATIC();
		main.setINVOKEINTERFACE(new MyObject());
			
		// call constructors in no particular order
		new Main(1);
		new Main(true);
		new Main("str");
		new Main("", "");
		new Main("", 0);
		new Main("", true);
		new Main((Thread) null, "string");
		new Main((Thread) null, 0);
	}

	// Be careful when initializing members.  If you instantiate an
	// object, then trivial constructors will become non-trivial.
	// Ex. Integer myInteger = new Integer(1); will cause Main() to be non-trivial.
	int myint;
	boolean mybool;
	private static int mystatic;
			
	// trivial constructor
	public Main() {
	}
			
	// constructors that just call super() are trivial
	public Main(Thread t, String str)
	{
		super(str);
	}
			
	// constructors that just call super() are usually trivial, but
	// this constructor uses a constant, so it is considered non-trivial.
	public Main(Thread t, int i)
	{
		super("string");
	}
			
	public Main(boolean bool) {
		// non-trivial conditional
		myint = bool ? 0 : 1;
	}
			
	public Main(int num) {
		// non-trivial switch
		switch(num) {
			default:
		}
	}
			
	public Main(String str) {
		// setting of statics is non-trivial
		mystatic = 2;
	}
		
	public Main(String str1, String str2)
	{
		// non-trivial method call
		privateMethod();
	}
			
	public Main(String str1, int i)
	{
		// non-trivial object construction
		new StringBuffer();
	}
			
	public Main(String str1, boolean bool)
	{
		// non-trivial this() call
		this(str1, 0);
	}
	

	// trivial getter
	public int getterTrivial() {
		return myint;
	}
		
	// trivial getter
	public boolean isBool() {
		return mybool;
	}
			
	// trivial getter
	public boolean hasBool() {
		return mybool;
	}
			
	// trivial setter
	public void setInt(int i) {
		myint = i;
	}
			
	// this would be trivial, but it is a getter that with no return value
	public void getVoid() {
	}
			
	// "empty" does not start with "get", "is", "has", or "set", so
	// it is considered non-trivial.
	private int empty() {
		return 0;
	}
			
	// this is a getter that takes a parameter, so it is non-trivial.
	public int getIntWithIntParm(int i) {
		return 0;
	}
		
	// this would be a trivial setter, but it does not have a parameter.
	public void set() {
	}
			
	// this would be a trivial setter, but it has more than one parameter.
	public void setIntWithTwoParms(int i, int j) {
		myint = i;
	}
			
	public int[][] getMultiDimArray() {
		// non-trivial construction of a multi-dimensional array
		return new int[1][1];
	}
			
	public void setIncrement(int i) {
		// non-trivial increment of local variable
		i++;
	}
			
	public void setConst(String str) {
		/*
		 * cause visitLdcInsn to be called because "str" is in the
		 * runtime constant pool.  An LDC operation is performed
		 * which is considered non-trivial.
		 */
		System.out.println("str");
	}
			
	public int[] getArray() {
		// causes visitIntInsn to be called.  Creating an array is a "single int operand".
		// non-trivial.
		return new int[1];
	}
			
	public Object getObject() {
		// causes visitTypeInsn to be called.  Creating an object is a type instruction.
		// non-trivial.
		return new Object();
	}
			
	public int getStatic() {
		// getting a static is non-trivial.
		return mystatic;
	}
			
	public void setStatic(int i) {
		// setting a static is non-trivial.
		mystatic = i;
	}
			
	// non-trivial local variable instruction (causes visitVarInsn(ISTORE)) (int store to local var)
	public void setISTORE(int i) {
		i = 0;
	}

	// non-trivial local variable instruction (causes visitVarInsn(LSTORE)) (long store to local var)
	public void setLSTORE(long l) {
		l = 0;
	}

	// non-trivial local variable instruction (causes visitVarInsn(FSTORE)) (floating store to local var)
	public void setFSTORE(float f) {
		f = 0;
	}

	// non-trivial local variable instruction (causes visitVarInsn(DSTORE)) (double store to local var)
	public void setDSTORE(double d) {
		d = 0;
	}

	// non-trivial local variable instruction (causes visitVarInsn(ASTORE)) (object store to local var)
	public void setASTORE(Object obj) {
		obj = null;
	}
			
	public void publicMethod() {
	}
	private void privateMethod() {
	}
	public static void staticMethod() {
	}
	
	// non-trivial public method call (causes visitMethodInsn(INVOKEVIRTUAL)) 
	public int getINVOKEVIRTUAL() {
		publicMethod();
		return 0;
	}

	// non-trivial private method call (causes visitMethodInsn(INVOKESPECIAL)) 
	public int getINVOKESPECIAL() {
		privateMethod();
		return 0;
	}

	// non-trivial static method call (causes visitMethodInsn(INVOKESTATIC)) 
	public int getINVOKESTATIC() {
		staticMethod();
		return 0;
	}

	// non-trivial interface method call (causes visitMethodInsn(INVOKEINTERFACE)) 
	public void setINVOKEINTERFACE(MyInterface obj) {
		obj.myInterfaceMethod();
	}
			


}
			"""

			testUtil.compileSource(ant, srcDir)
			
			testUtil.instrumentClasses(ant, srcDir, datafile, instrumentDir, [ignoretrivial:true])
			
			/*
			 * Kick off the Main (instrumented) class.
			 */
			ant.java(classname:'mypackage.Main', dir:srcDir, fork:true, failonerror:true) {
				classpath {
					dirset(dir:instrumentDir)
					dirset(dir:srcDir)
					dirset(dir:testUtil.coberturaClassDir)
				}
			}

			/*
			 * Now create a cobertura xml file and make sure the correct counts are in it.
			 */
			ant.'cobertura-report'(datafile:datafile, format:'xml', destdir:srcDir)
			dom = TestUtil.getXMLReportDOM("${srcDir}/coverage.xml")
				
			// trivial empty constructor
			assertIgnored('<init>', '()V')

			// trivial constructor Main(Thread, String) that just calls super()
			assertIgnored('<init>', '(Ljava/lang/Thread;Ljava/lang/String;)V')

			// trivial getter
			assertIgnored('getterTrivial')

   			// isBool is trivial
			assertIgnored('isBool')

   			// hasBool is trivial
			assertIgnored('hasBool')

   			// setInt is trivial
			assertIgnored('setInt')

			
			// Main(int) has non-trivial switch
			assertNotIgnored('<init>', '(I)V')

			// Main(boolean) has non-trivial conditional
			assertNotIgnored('<init>', '(Z)V')

			// "empty" does not start with "get", "is", "has", or "set".
			assertNotIgnored('empty')

   			// gets with no return are considered non-trivial
			assertNotIgnored('getVoid')

   			// gets that have parameters are considered non-trivial
   			assertNotIgnored('getIntWithIntParm')

   			// sets that have no parameters are considered non-trivial
			assertNotIgnored('set')
			
   			// sets that have more than one parameters are considered non-trivial
			assertNotIgnored('setIntWithTwoParms')
			
			// don't ignore methods with multi-dimensional array creates
			assertNotIgnored('getMultiDimArray')

			// don't ignore methods with increment instructions for local variables
			assertNotIgnored('setIncrement')

			// don't ignore methods with LDC instructions (that use constants from the runtime pool)
			assertNotIgnored('setConst')
			assertNotIgnored('<init>', '(Ljava/lang/Thread;I)V') // Main(Thread, int)
		
			// don't ignore methods with a single int operand (like creating an array).
			assertNotIgnored('getArray')

			// don't ignore methods with type instructions (like creating an object).
			assertNotIgnored('getObject')

			// don't ignore methods that use statics.
			assertNotIgnored('getStatic')
			assertNotIgnored('setStatic')
			assertNotIgnored('<init>', '(Ljava/lang/String;)V')
			
			// non-trivial local variable instructions (causes visitVarInsn call)
			assertNotIgnored('setISTORE')
			assertNotIgnored('setLSTORE')
			assertNotIgnored('setFSTORE')
			assertNotIgnored('setDSTORE')
			assertNotIgnored('setASTORE')
			
			// non-trivial method calls
			assertNotIgnored('getINVOKEVIRTUAL')
			assertNotIgnored('getINVOKESPECIAL')
			assertNotIgnored('getINVOKESTATIC')
			assertNotIgnored('setINVOKEINTERFACE')
			assertNotIgnored('<init>', '(Ljava/lang/String;Ljava/lang/String;)V') // Main(String, String)
			assertNotIgnored('<init>', '(Ljava/lang/String;I)V') // Main(String, int)
			assertNotIgnored('<init>', '(Ljava/lang/String;Z)V') // Main(String, boolean)
		}
	}

	def assertIgnored(methodName, signature=null) {
		def lines = TestUtil.getLineCounts(dom, 'mypackage.Main', methodName, signature)
		def methodDesc = "$methodName${signature ? '(' + signature + ')' : ''}"
		assertEquals("$methodDesc not ignored", 0, lines.size);
	}

	def assertNotIgnored(methodName, signature=null) {
		def lines = TestUtil.getLineCounts(dom, 'mypackage.Main', methodName, signature)
		def methodDesc = "$methodName${signature ? '(' + signature + ')' : ''}"
		assertTrue("$methodDesc should not be ignored", lines.size > 0);
		assertTrue("$methodDesc should not be ignored", lines[0].hits > 0)
	}
}
