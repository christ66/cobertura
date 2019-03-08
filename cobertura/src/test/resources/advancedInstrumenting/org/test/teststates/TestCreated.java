package org.test.teststates;

public final class TestCreated extends TestPending {

    public static final TestCreated INSTANCE = new TestCreated("TestCreated");

    private static java.util.List<TestState> SUBSTATES; 

    public static java.util.List<TestState> getSubstates() {
        if (SUBSTATES == null) {
            java.util.List<TestState> tmpSubstates = new java.util.ArrayList<TestState>();
            SUBSTATES = tmpSubstates;
        }
        return SUBSTATES;
    }

    private TestCreated(String name) {
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