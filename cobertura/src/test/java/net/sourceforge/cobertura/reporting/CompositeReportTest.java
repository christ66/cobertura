package net.sourceforge.cobertura.reporting;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompositeReportTest {

	private CompositeReport report;

	@Before
	public void setUp() throws Exception {
		report = new CompositeReport();
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals(ReportName.COMPOSITE_REPORT, report.getName());
	}

	@Test
	public void testGetByName() throws Exception {
		assertEquals(report.getClass(), report.getByName(
				ReportName.COMPOSITE_REPORT).getClass());
		assertEquals(NullReport.class, report.getByName(null).getClass());
	}

	@Test
	public void testAddReport() throws Exception {
		Report thresholdReport = new CoverageThresholdsReport(null);
		assertEquals(NullReport.class, report.getByName(
				ReportName.THRESHOLDS_REPORT).getClass());
		report.addReport(thresholdReport);
		assertEquals(CoverageThresholdsReport.class, report.getByName(
				ReportName.THRESHOLDS_REPORT).getClass());
	}
}
