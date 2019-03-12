package org.test.teststates;

public final class TestDone extends TestState {
    public static final TestDone INSTANCE = new TestDone("TestDone");

    private static java.util.List<TestState> SUBSTATES; 

    public static java.util.List<TestState> getSubstates() {
        if (SUBSTATES == null) {
            java.util.List<TestState> tmpSubstates = new java.util.ArrayList<TestState>();
            SUBSTATES = tmpSubstates;
        }
        return SUBSTATES;
    }

    private TestDone(String name) {
        super(name);
    }

	static {
		// ensure that all instances are actually created when this class is loaded.
		final Object[] dummy = new Object [] {
			TestRunning.INSTANCE,
			TestDone.INSTANCE,
			TestCreated.INSTANCE,
			null
        };
    }
}
