package net.sourceforge.cobertura.check;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static net.sourceforge.cobertura.check.CoverageResultEntry.CoverageLevel.PACKAGE;
import static net.sourceforge.cobertura.check.CoverageResultEntry.CoverageType.BRANCH;
import static org.junit.Assert.assertEquals;

public class CoverageResultEntryTest {
	private static final CoverageResultEntry.CoverageLevel COVERAGE_LEVEL = PACKAGE;
	private static final CoverageResultEntry.CoverageType COVERAGE_TYPE = BRANCH;
	private static final String NAME = "some.name";
	private static final int CURRENT_COVERAGE = 10;
	private static final int EXPECTED_COVERAGE = 20;
	private CoverageResultEntry entry;

	@Before
	public void setUp() throws Exception {
		entry = new CoverageResultEntry(COVERAGE_LEVEL, COVERAGE_TYPE, NAME,
				CURRENT_COVERAGE, EXPECTED_COVERAGE);
	}

	@Test
	public void testGetCoverageLevel() throws Exception {
		assertEquals(COVERAGE_LEVEL, entry.getCoverageLevel());
	}

	@Test
	public void testGetCoverageType() throws Exception {
		assertEquals(COVERAGE_TYPE, entry.getCoverageType());
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals(NAME, entry.getName());
	}

	@Test
	public void testGetCurrentCoverage() throws Exception {
		assertEquals(CURRENT_COVERAGE, entry.getCurrentCoverage(), 0);
	}

	@Test
	public void testGetExpectedCoverage() throws Exception {
		assertEquals(EXPECTED_COVERAGE, entry.getExpectedCoverage(), 0);
	}

	@Test
	public void testIsBelowExpectedCoverage_true() throws Exception {
		assertTrue(entry.isBelowExpectedCoverage());
	}

	@Test
	public void testIsBelowExpectedCoverage_false() throws Exception {
		entry = new CoverageResultEntry(COVERAGE_LEVEL, COVERAGE_TYPE, NAME,
				EXPECTED_COVERAGE + 10, EXPECTED_COVERAGE);
		assertFalse(entry.isBelowExpectedCoverage());
	}
}
