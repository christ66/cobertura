package test.condition;

import junit.framework.TestCase;

public class TestUnit extends TestCase {

	public void testMethod() {
		IgnoreMeAlso.foo();
		IgnoreMeNot.foo();
		IgnoreMe.foo();
	}
}
