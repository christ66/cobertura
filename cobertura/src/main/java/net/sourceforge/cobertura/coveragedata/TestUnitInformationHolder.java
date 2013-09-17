/**
 * 
 */
package net.sourceforge.cobertura.coveragedata;

import java.util.ArrayList;
import java.util.List;

/**
 * @author christ66
 *
 */
public class TestUnitInformationHolder {
	private List<String> testNames;
	
	public TestUnitInformationHolder() {
		testNames = new ArrayList<String>();
	}
	
	public void appendTestUnit(String nameOfTestUnit) {
		testNames.add(nameOfTestUnit);
	}
	
	public int getNumOfExecutions() {
		return testNames.size();
	}

	public List<String> getAndReset() {
		List<String> returnArray = testNames;
		testNames = new ArrayList<String>();
		return returnArray;
	}

	public List<String> getTestUnitList() {
		return testNames;
	}
}
