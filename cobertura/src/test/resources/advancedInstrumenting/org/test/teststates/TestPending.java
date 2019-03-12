package org.test.teststates;

public abstract class TestPending extends TestState {

    private static java.util.List<TestState> SUBSTATES; 

    public static java.util.List<TestState> getSubstates() {
        if (SUBSTATES == null) {
            java.util.List<TestState> tmpSubstates = new java.util.ArrayList<TestState>();
                tmpSubstates.add(org.test.teststates.TestRunning.INSTANCE);
                tmpSubstates.add(org.test.teststates.TestCreated.INSTANCE);
            SUBSTATES = tmpSubstates;
        }
        return SUBSTATES;
    }

    protected TestPending(String name) {
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
