/**
 * 
 */
package net.sourceforge.cobertura.ant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author christ66
 *
 */
public class TestUnits {

	List<String> testNames = new ArrayList<String>();

	public void incrementExection(String nameOfTestUnit) {
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
}
