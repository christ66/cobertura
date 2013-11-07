package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.dsl.ReportFormat;

public class NullReport implements Report {

	public void export(ReportFormat reportFormat) {
		//no action performed
	}

	public ReportName getName() {
		return ReportName.NULL_REPORT;
	}

	public Report getByName(ReportName name) {
		return this;
	}
}
