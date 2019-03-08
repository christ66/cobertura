package test.depr;

import org.junit.Test;

public class DeprecatedTestUnit {
	@Test
	public void testMethod() {
		IgnoreMeAlso.foo();
		IgnoreMeNot.foo();
		IgnoreMe.foo();
	}
}
