package net.sourceforge.cobertura.check;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoverageThresholdTest {

	private static final double MIN_LINE_PERCENTAGE = .5;
	private static final double MIN_BRANCH_PERCENTAGE = .5;
	private static final String REGEX = "*";
	private static final double DELTA = .001;
	private CoverageThreshold coverageThreshold;

	@Before
	public void setUp() throws Exception {
		this.coverageThreshold = new CoverageThreshold(REGEX,
				MIN_BRANCH_PERCENTAGE, MIN_LINE_PERCENTAGE);
	}

	@Test
	public void testGetRegex() throws Exception {
		assertEquals(REGEX, coverageThreshold.getRegex());
	}

	@Test
	public void testGetMinBranchPercentage() throws Exception {
		assertEquals(MIN_BRANCH_PERCENTAGE, coverageThreshold
				.getMinBranchPercentage(), DELTA);
	}

	@Test
	public void testGetMinLinePercentage() throws Exception {
		assertEquals(MIN_LINE_PERCENTAGE, coverageThreshold
				.getMinLinePercentage(), DELTA);
	}
}
