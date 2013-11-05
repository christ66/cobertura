package net.sourceforge.cobertura.dsl;

import net.sourceforge.cobertura.check.CheckCoverageTask;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.instrument.CodeInstrumentationTask;
import net.sourceforge.cobertura.merge.MergeProjectDataFilesTask;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.CompositeReport;
import net.sourceforge.cobertura.reporting.NativeReport;
import net.sourceforge.cobertura.reporting.Report;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.sourceforge.cobertura.coveragedata.TouchCollector.applyTouchesOnProjectData;

/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

public class Cobertura {

	private Arguments args;
	private ProjectData projectData;
	private CodeInstrumentationTask instrumentationTask;
	private CheckCoverageTask checkCoverageTask;
	private MergeProjectDataFilesTask mergeProjectDataFilesTask;

	private AtomicBoolean didApplyInstrumentationResults;

	private CompositeReport report;

	/*
	 * Private constructor so we get sure Cobertura
	 * is always initialized with Arguments
	 */
	private Cobertura() {
		report = new CompositeReport();
	}

	public Cobertura(Arguments arguments) {
		args = arguments;
		instrumentationTask = new CodeInstrumentationTask();
		checkCoverageTask = new CheckCoverageTask();
		mergeProjectDataFilesTask = new MergeProjectDataFilesTask();

		didApplyInstrumentationResults = new AtomicBoolean(false);
	}

	/**
	 * Instruments the code. Should be invoked after compiling.
	 * Classes to be instrumented are taken from constructor args
	 * @return this Cobertura instance
	 * @throws Throwable
	 */
	public Cobertura instrumentCode() throws Throwable {
		instrumentationTask.instrument(args, getProjectDataInstance());
		return this;
	}

	/**
	 * This should be invoked after running tests.
	 * @return this Cobertura instance
	 */
	public Cobertura calculateCoverage() {
		applyTouchesOnProjectData(projectData);
		didApplyInstrumentationResults.set(true);
		return this;
	}

	/**
	 * Checks metrics values against thresholds
	 * @return this Cobertura instance
	 */
	public Cobertura checkThresholds() {
		report.addReport(checkCoverageTask.checkCoverage(args,
				getProjectDataInstance()));
		return this;
	}

	/**
	 * Merges specified project data files as specified on arguments;
	 * @return this Cobertura instance
	 */
	public Cobertura merge() {
		mergeProjectDataFilesTask.mergeProjectDataFiles(args);
		return this;
	}

	/**
	 * Creates a report with coverage and metrics data
	 * @return Report instance, never <code>null</code>
	 */
	public Report report() {
		//		if (!didApplyInstrumentationResults.get()) {
		//			calculateCoverage();
		//		}

		ComplexityCalculator complexityCalculator = new ComplexityCalculator(
				args.getSources());

		report.addReport(new NativeReport(getProjectDataInstance(), args
				.getDestinationDirectory(), args.getSources(),
				complexityCalculator, args.getEncoding()));

		return report;
	}

	/**
	 * Serializes project data to file specified in constructor args
	 * @return this Cobertura instance
	 */
	public Cobertura saveProjectData() {
		CoverageDataFileHandler.saveCoverageData(getProjectDataInstance(), args
				.getDataFile());
		return this;
	}

	/*  Aux methods  */
	private ProjectData getProjectDataInstance() {
		// Load project data; see notes at the beginning of CodeInstrumentationTask class
		if (projectData != null) {
			return projectData;
		}
		if (args.getDataFile().isFile())
			projectData = CoverageDataFileHandler.loadCoverageData(args
					.getDataFile());
		if (projectData == null)
			projectData = new ProjectData();

		return projectData;
	}
}
