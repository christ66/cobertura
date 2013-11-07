package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.check.CoverageResultEntry;
import net.sourceforge.cobertura.dsl.ReportFormat;

import java.util.Collections;
import java.util.List;

/**
 * Contains coverage data.
 */
public class CoverageThresholdsReport implements Report {
	private List<CoverageResultEntry> coverageResultEntries;
	private NullReport nullReport;

	public CoverageThresholdsReport(
			List<CoverageResultEntry> coverageResultEntries) {
		this.coverageResultEntries = Collections
				.unmodifiableList(coverageResultEntries);
		this.nullReport = new NullReport();
	}

	public void export(ReportFormat reportFormat) {
		//TODO left for future implementations
	}

	public ReportName getName() {
		return ReportName.THRESHOLDS_REPORT;
	}

	public Report getByName(ReportName name) {
		if (getName().equals(name)) {
			return this;
		}
		return nullReport;
	}

	public List<CoverageResultEntry> getCoverageResultEntries() {
		return coverageResultEntries;
	}
}
