package net.sourceforge.cobertura.jdk;

import groovy.util.Node;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author stevenchristou
 *         Date: 3/10/16
 *         Time: 1:10 AM
 */
public class JDK8Test {
    static Node dom;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException,
            ParserConfigurationException, SAXException {
        dom = JDKUtils.setUpBeforeClass(java8TestFile, "1.8", "mypackage.Java8TestCase", "mypackage/Java8TestCase.java");
    }

    /**
     *    public void lambda_basic() throws Exception {
     *        Runnable runnable = () -> { System.err.println ("test"); };
     *    }
     */
    @Test
    public void testBasicLambda() {
        int hitCount = TestUtils.getTotalHitCount(dom,
                "mypackage.Java8TestCase", "lambda_basic");
        assertEquals(1, hitCount);
    }

    @Test
    public void testMain() throws Exception {
        int hitCount = TestUtils.getTotalHitCount(dom,
                "mypackage.Java8TestCase", "main");
        assertEquals(3, hitCount);
    }

    static final String java8TestFile =
              "\n package mypackage;"
            + "\n "
            + "\n import java.util.*;"
            + "\n import java.io.*;"
            + "\n "
            + "\n public class Java8TestCase {"
            + "\n "
            + "\n   public static void main (String[] args) throws Exception {"
            + "\n     Java8TestCase t = new Java8TestCase();"
            + "\n     t.lambda_basic();"
            + "\n   }"
            + "\n   "
            + "\n   public void lambda_basic() throws Exception {"
            + "\n     Runnable runnable = () -> { System.err.println(\"test\");};"
            + "\n   }"
            + "\n }";

}
