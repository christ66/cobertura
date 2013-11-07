package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.check.CoverageResultEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class CoverageThresholdsReportTest {

	private ArrayList<CoverageResultEntry> coverageResultEntries = new ArrayList<CoverageResultEntry>();
	private CoverageThresholdsReport report;

	@Before
	public void setUp() throws Exception {
		this.report = new CoverageThresholdsReport(coverageResultEntries);
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals(ReportName.THRESHOLDS_REPORT, report.getName());
	}

	@Test
	public void testGetByName() throws Exception {
		assertEquals(report.getClass(), report.getByName(
				ReportName.THRESHOLDS_REPORT).getClass());
		assertEquals(NullReport.class, report.getByName(null).getClass());
	}

	@Test
	public void testGetCoverageResultEntries() throws Exception {
		assertEquals(coverageResultEntries, report.getCoverageResultEntries());
	}
}
