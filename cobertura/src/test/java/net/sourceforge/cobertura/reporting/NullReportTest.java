package net.sourceforge.cobertura.reporting;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NullReportTest {

	private NullReport report;

	@Before
	public void setUp() throws Exception {
		this.report = new NullReport();
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals(ReportName.NULL_REPORT, report.getName());
	}

	@Test
	public void testGetByName() throws Exception {
		assertEquals(report, report.getByName(ReportName.NULL_REPORT));
		assertEquals(report, report.getByName(null));
	}
}
