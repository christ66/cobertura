/**
 * 
 */
package net.sourceforge.cobertura;

/**
 * This is used at test unit manipulation time.
 * We check all the test units. All methods will receive the following:
 * <code>
 * net.sourceforge.cobertura.Cobertura.TestClassAndMethodNamesMerged = "TESTCLASS - METHODNAME";
 * </code>
 * before each individual test. This will allow for us
 * to obtain the test unit name very quickly without
 * having to do reflection or stack trace searching.
 * 
 * 
 * @author christ66
 *
 */
public class Cobertura {
	public static String TestClassAndMethodNamesMerged = "";
}
