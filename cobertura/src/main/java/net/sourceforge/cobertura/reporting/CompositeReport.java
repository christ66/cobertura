package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.dsl.ReportFormat;

import java.util.HashSet;
import java.util.Set;

public class CompositeReport implements Report {

	private Set<Report> reports;

	public CompositeReport() {
		reports = new HashSet<Report>();
		reports.add(new NullReport());
	}

	public void export(ReportFormat reportFormat) {
		for (Report report : reports) {
			report.export(reportFormat);
		}
	}

	public ReportName getName() {
		return ReportName.COMPOSITE_REPORT;
	}

	public Report getByName(ReportName name) {
		for (Report report : reports) {
			if (report.getName().equals(name)) {
				return report;
			}
		}
		return getByName(ReportName.NULL_REPORT);
	}

	public void addReport(Report report) {
		reports.add(report);
	}
}
