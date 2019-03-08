package net.sourceforge.cobertura.jdk;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import groovy.util.Node;
import net.sourceforge.cobertura.test.util.TestUtils;

public class JDK7Test {
    static Node dom;

    @BeforeClass
	public static void setUpBeforeClass() throws IOException,
			ParserConfigurationException, SAXException {
        dom = JDKUtils.setUpBeforeClass(java7TestFile, "1.7", "mypackage.Java7TestCase", "mypackage/Java7TestCase.java");
	}

	/**
	 *    public void try_with_resource() throws Exception {
	 *      try (FileOutputStream fos = new FileOutputStream(\"test.txt\");
	 *           FileOutputStream fos2 = new FileOutputStream(\"meow.txt\")) {
	 *        ....
	 *      }
	 *    }
	 */
	@Test
	public void testTryWithResource() {
		int hitCount = TestUtils.getTotalHitCount(dom,
				"mypackage.Java7TestCase", "try_with_resource");
		assertEquals(5, hitCount);
	}

	/**
	 *    public void diamond_operator() {
	 *      Map<String, List<String>> stringMaps = new TreeMap <> ();
	 *    }
	 */
	@Test
	public void testDiamondOperator() {
		int hitCount = TestUtils.getTotalHitCount(dom,
				"mypackage.Java7TestCase", "diamond_operator");
		assertEquals(2, hitCount);
	}

	/**
	 * public void string_in_switch() {
	 *   String x = "asdfg";
	 *   switch(x) {
	 *     case "asdf":
	 *       break;
	 *     default:
	 *       break;
	 *   }
	 * }
	 */
	@Test
	public void testStringInSwitch() {
		int hitCount = TestUtils.getTotalHitCount(dom,
				"mypackage.Java7TestCase", "string_in_switch");
		assertEquals(3, hitCount);
	}

	/**
	 *   public void numerical_literals_underscores() {
	 *     int thousand = 1_000;
	 *     int million  = 1_000_000;
	 *   }
	 */
	@Test
	public void testNumericalLteralsUnderscores() {
		int hitCount = TestUtils.getTotalHitCount(dom,
				"mypackage.Java7TestCase", "numerical_literals_underscores");
		assertEquals(3, hitCount);
	}

	/**
	 *   public void multi_catch() {
	 *     try {
	 *       FileOutputStream fos = new FileOutputStream(\"test.txt\");
	 *       int a = 5/0;
	 *       } catch (IOException | ArithmeticException e){
	 *     }
	 *   }
	 */
	@Test
	public void testMultiCatch() {
		int hitCount = TestUtils.getTotalHitCount(dom,
				"mypackage.Java7TestCase", "multi_catch");
		assertEquals(4, hitCount);
	}

    /**
     *    public void binary_literals() {
     *      int yesThisIsAnInt = 0b10100001010001011010000101000101;
     *    }
     */
    @Test
    public void testBinaryLiteral() {
        int hitCount = TestUtils.getTotalHitCount(dom,
                "mypackage.Java7TestCase", "binary_literals");
        assertEquals(2, hitCount);
    }

	@Test
	public void testMain() throws Exception {
		int hitCount = TestUtils.getTotalHitCount(dom,
				"mypackage.Java7TestCase", "main");
		assertEquals(8, hitCount);
	}

	static final String java7TestFile = "\n package mypackage;"
			+ "\n "
			+ "\n import java.util.*;"
			+ "\n import java.io.*;"
			+ "\n "
			+ "\n public class Java7TestCase {"
			+ "\n "
			+ "\n   public static void main (String[] args) throws Exception {"
			+ "\n     Java7TestCase t = new Java7TestCase();"
			+ "\n     t.try_with_resource();"
			+ "\n     t.diamond_operator();"
			+ "\n     t.string_in_switch();"
			+ "\n     t.numerical_literals_underscores();"
            + "\n     t.binary_literals();"
			+ "\n     t.multi_catch();"
			+ "\n   }"
			+ "\n   "
			+ "\n   public void try_with_resource() throws Exception {"
			+ "\n     try (FileOutputStream fos = new FileOutputStream(\"test.txt\");"
			+ "\n          FileOutputStream fos2 = new FileOutputStream(\"meow.txt\")) {"
			+ "\n     }"
			+ "\n   }"
			+ "\n "
			+ "\n   public void diamond_operator() {"
			+ "\n     Map<String, List<String>> stringMaps = new TreeMap <> ();"
			+ "\n   }"
			+ "\n "
			+ "\n   public void string_in_switch() {"
			+ "\n     String x = \"asdfg\";"
			+ "\n     switch(x) {"
			+ "\n       case \"asdf\":"
			+ "\n         break;"
			+ "\n       default:"
			+ "\n         break;"
			+ "\n     }"
			+ "\n   }"
			+ "\n "
			+ "\n   public void numerical_literals_underscores() {"
			+ "\n     int thousand = 1_000;"
			+ "\n     int million  = 1_000_000;"
			+ "\n   }"
            + "\n "
            + "\n   public void binary_literals() {"
            + "\n     int yesThisIsAnInt = 0b10100001010001011010000101000101;"
            + "\n   }"
			+ "\n   "
			+ "\n   public void multi_catch() {"
			+ "\n     try {"
			+ "\n       FileOutputStream fos = new FileOutputStream(\"test.txt\");"
			+ "\n       int a = 5/0;"
			+ "\n     } catch (IOException | ArithmeticException e){"
			+ "\n     }" + "\n   }" + "\n }";
}
