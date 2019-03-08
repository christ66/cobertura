package net.sourceforge.cobertura.bugs;

import static org.junit.Assert.*;

import groovy.util.Node;

import org.junit.Test;

import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;
import net.sourceforge.cobertura.test.util.TestUtils;

public class InstrumentationBug1Test extends AbstractCoberturaTestCase {

	Node dom;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		dom = super.createAndExecuteMainMethod(packageName, fileName, file1,
				mainMethod);
	}

	@Test
	public void testIssue() {
		assertEquals(1, TestUtils.getTotalHitCount(dom,
				"mypackage.CoverageBug",
				"<clinit>"));
		assertEquals(2, TestUtils.getTotalHitCount(dom,
				"mypackage.CoverageBug",
				"<init>"));
		assertEquals(3, TestUtils.getTotalHitCount(dom,
				"mypackage.CoverageBug",
				"main"));
		assertEquals(2, TestUtils.getTotalHitCount(dom, "mypackage.TestObject",
				"<init>"));
		assertEquals(2, TestUtils.getTotalHitCount(dom, "mypackage.CoverageBug",
				"instanceMethod"));
		assertEquals(4, TestUtils.getTotalHitCount(dom, "mypackage.CoverageBug",
				"testMethod"));

	}

	static final String packageName = "mypackage";
	static final String fileName = "CoverageBug";
	static final String mainMethod = "mypackage.CoverageBug";
	static final String file1 = "package mypackage;"
			+ "public class CoverageBug {\n" +
"\n" +
"\n" +
"    public static Object object = new Object();\n" +
"\n" +
"    public void instanceMethod(Object o) {\n" +
"    }\n" +
"\n" +
"    public  boolean testMethod(boolean a) {\n" +
"        instanceMethod(new TestObject(a ? 0 : 1));\n" +
"        return a;\n" +
"    }\n" +
"\n" +
"\n" +
"    public static void main(String args[]) {\n" +
"        new CoverageBug().testMethod(true);\n" +
"        new CoverageBug().testMethod(false);\n" +
"    }\n" +
"}\n" +
"\n" +
"class TestObject {\n" +
"\n" +
"    public TestObject(int i) {}\n" +
"}";
}