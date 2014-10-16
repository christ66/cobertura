package org.test.tesstates;

import java.util.List;
import java.util.Map;

public abstract class TestState {

    private static java.util.Map<String, TestState> SUBSTATES;

    private static java.util.List<TestState> SUBSTATE_LIST;

    private static java.util.Map<String, TestState> getSubstateMap() {
        if (SUBSTATES == null) {
            java.util.Map<String, TestState> tmpSubstates = new java.util.HashMap<String, TestState>();
            tmpSubstates.put(org.test.tesstates.TestRunning.INSTANCE.getName(), org.test.tesstates.TestRunning.INSTANCE);
            tmpSubstates.put(org.test.tesstates.TestDone.INSTANCE.getName(), org.test.tesstates.TestDone.INSTANCE);
            tmpSubstates.put(org.test.tesstates.TestCreated.INSTANCE.getName(), org.test.tesstates.TestCreated.INSTANCE);
            SUBSTATES = tmpSubstates;
        }
        return SUBSTATES;
    }

    public static java.util.List<TestState> getSubstates() {
        if (SUBSTATE_LIST == null) {
            SUBSTATE_LIST = new java.util.ArrayList<TestState>(getSubstateMap().values());
        }
        return SUBSTATE_LIST;
    }

    public static TestState getState(String name) {
        return getSubstateMap().get(name);
    }

    public static List<TestState> getStateList() {
        return getSubstates();
    }

    public static Map<String, TestState> getStateMap() {
        return getSubstateMap();
    }

    private String name;

    public String getName() {
        return this.name;
    }

    protected TestState(String name) {
        this.name = name;
    }

    static {
        // ensure that all instances are actually created when this class is loaded.
        final Object[] dummy = new Object [] {
            TestRunning.INSTANCE,
            TestDone.INSTANCE,
            TestCreated.INSTANCE,
            null
        };
        // Avoid compiler warning
        dummy.getClass();
    }
}
