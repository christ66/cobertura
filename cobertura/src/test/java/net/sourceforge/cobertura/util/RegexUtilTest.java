package net.sourceforge.cobertura.util;

import java.util.Collection;
import java.util.Vector;

import junit.framework.TestCase;

public class RegexUtilTest extends TestCase {
	public final void testRegexProcessing() {
		Collection regex = new Vector();
		RegexUtil.addRegex(regex, "^fo.*o");
		RegexUtil.addRegex(regex, "^ba.*r");
		assertTrue(RegexUtil.matches(regex, "fooo"));
		assertFalse(RegexUtil.matches(regex, "foobar"));
	}
}