package net.sourceforge.cobertura.check;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.reporting.CoverageThresholdsReport;
import net.sourceforge.cobertura.reporting.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import static net.sourceforge.cobertura.check.CoverageResultEntry.CoverageLevel.*;
import static net.sourceforge.cobertura.check.CoverageResultEntry.CoverageType.BRANCH;
import static net.sourceforge.cobertura.check.CoverageResultEntry.CoverageType.LINE;

/*
 * Cobertura - http://cobertura.sourceforge.net/
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

/**
 * Checks if project meets the required coverage thresholds.
 */
public class CheckCoverageTask {

<<<<<<< HEAD
	private static final Logger logger = LoggerFactory
			.getLogger(CheckCoverageMain.class);

	private final Perl5Matcher pm = new Perl5Matcher();

	private final Perl5Compiler pc = new Perl5Compiler();
=======
	private static final Logger logger = Logger
			.getLogger(CheckCoverageMain.class);

	final Perl5Matcher pm = new Perl5Matcher();

	final Perl5Compiler pc = new Perl5Compiler();
>>>>>>> Fix failing tests

	/**
	 * The default CoverageRate needed for a class to pass the check.
	 */
	private CoverageRate minimumCoverageRate;

	/**
	 * The keys of this map contain regular expression Patterns that
	 * match against classes.  The values of this map contain
	 * CoverageRate objects that specify the minimum coverage rates
	 * needed for a class that matches the pattern.
	 */
	private Map<Pattern, CoverageRate> minimumCoverageRates = new HashMap<Pattern, CoverageRate>();

	/**
	 * The keys of this map contain package names. The values of this
	 * map contain PackageCoverage objects that track the line and
	 * branch coverage values for a package.
	 */
	private Map<String, PackageCoverage> packageCoverageMap = new HashMap<String, PackageCoverage>();

	/**
	 * This method returns the CoverageRate object that
	 * applies to the given class.  If checks if there is a
	 * pattern that matches the class name, and returns that
	 * if it finds one.  Otherwise it uses the global minimum
	 * rates that were passed in.
	 */
	private CoverageRate findMinimumCoverageRate(String classname) {
		for (Map.Entry<Pattern, CoverageRate> patternCoverageRateEntry : this.minimumCoverageRates
				.entrySet()) {
			Map.Entry entry = (Map.Entry) patternCoverageRateEntry;

			if (pm.matches(classname, (Pattern) entry.getKey())) {
				return (CoverageRate) entry.getValue();
			}
		}
		return this.minimumCoverageRate;
	}

	public Report checkCoverage(Arguments arguments, ProjectData projectData) {
		File dataFile = CoverageDataFileHandler.getDefaultDataFile();
		double branchCoverageRate;
		double lineCoverageRate;
		double packageBranchCoverageRate;
		double packageLineCoverageRate;
		double totalBranchCoverageRate;
		double totalLineCoverageRate;

		branchCoverageRate = arguments.getClassBranchThreshold();
		dataFile = arguments.getDataFile();
		lineCoverageRate = arguments.getClassLineThreshold();
		for (CoverageThreshold threshold : arguments
				.getMinimumCoverageThresholds()) {
			try {
				this.minimumCoverageRates.put(pc.compile(threshold.getRegex()),
						new CoverageRate(threshold.getMinBranchPercentage(),
								threshold.getMinLinePercentage()));
			} catch (MalformedPatternException e) {
				logger.error(String.format("Got malformed regex expression %s",
						threshold.getRegex()), e);
			}
		}
		packageBranchCoverageRate = arguments.getPackageBranchThreshold();
		packageLineCoverageRate = arguments.getPackageLineThreshold();
		totalBranchCoverageRate = arguments.getTotalBranchThreshold();
		totalLineCoverageRate = arguments.getTotalLineThreshold();

		// If they didn't specify any thresholds, then use some defaults
		if ((branchCoverageRate == -1.0) && (lineCoverageRate == -1.0)
				&& (packageLineCoverageRate == -1.0)
				&& (packageBranchCoverageRate == -1.0)
				&& (totalLineCoverageRate == -1.0)
				&& (totalBranchCoverageRate == -1.0)
				&& (this.minimumCoverageRates.size() == 0)) {
			branchCoverageRate = 0.5;
			lineCoverageRate = 0.5;
			packageBranchCoverageRate = 0.5;
			packageLineCoverageRate = 0.5;
			totalBranchCoverageRate = 0.5;
			totalLineCoverageRate = 0.5;
		}
		// If they specified one or more thresholds, default everything else to 0
		else {
			if (branchCoverageRate == -1.0)
				branchCoverageRate = 0.0;
			if (lineCoverageRate == -1.0)
				lineCoverageRate = 0.0;
			if (packageLineCoverageRate == -1.0)
				packageLineCoverageRate = 0.0;
			if (packageBranchCoverageRate == -1.0)
				packageBranchCoverageRate = 0.0;
			if (totalLineCoverageRate == -1.0)
				totalLineCoverageRate = 0.0;
			if (totalBranchCoverageRate == -1.0)
				totalBranchCoverageRate = 0.0;
		}

		this.minimumCoverageRate = new CoverageRate(lineCoverageRate,
				branchCoverageRate);

		double totalLines = 0;
		double totalLinesCovered = 0;
		double totalBranches = 0;
		double totalBranchesCovered = 0;

		Iterator iter = projectData.getClasses().iterator();
		List<CoverageResultEntry> coverageResultEntries = new ArrayList<CoverageResultEntry>();
		while (iter.hasNext()) {
			ClassData classData = (ClassData) iter.next();
			CoverageRate coverageRate = findMinimumCoverageRate(classData
					.getName());

			if (totalBranchCoverageRate > 0.0) {
				totalBranches += classData.getNumberOfValidBranches();
				totalBranchesCovered += classData.getNumberOfCoveredBranches();
			}

			if (totalLineCoverageRate > 0.0) {
				totalLines += classData.getNumberOfValidLines();
				totalLinesCovered += classData.getNumberOfCoveredLines();
			}

			PackageCoverage packageCoverage = getPackageCoverage(classData
					.getPackageName());
			if (packageBranchCoverageRate > 0.0) {
				packageCoverage.addBranchCount(classData
						.getNumberOfValidBranches());
				packageCoverage.addBranchCoverage(classData
						.getNumberOfCoveredBranches());
			}

			if (packageLineCoverageRate > 0.0) {
				packageCoverage.addLineCount(classData.getNumberOfValidLines());
				packageCoverage.addLineCoverage(classData
						.getNumberOfCoveredLines());
			}

			logger.debug("Class " + classData.getName()
					+ ", line coverage rate: "
					+ percentage(classData.getLineCoverageRate())
					+ "%, branch coverage rate: "
					+ percentage(classData.getBranchCoverageRate()) + "%");

			coverageResultEntries.add(new CoverageResultEntry(CLASS, BRANCH,
					classData.getName(), classData.getBranchCoverageRate(),
					coverageRate.getBranchCoverageRate()));

			coverageResultEntries.add(new CoverageResultEntry(CLASS, LINE,
					classData.getName(), classData.getLineCoverageRate(),
					coverageRate.getLineCoverageRate()));
		}

		coverageResultEntries.addAll(checkPackageCoverageLevels(
				packageBranchCoverageRate, packageLineCoverageRate));

		// Check the rates for the overall project
		coverageResultEntries.add(new CoverageResultEntry(PROJECT, BRANCH,
				"project", totalBranchesCovered / totalBranches,
				totalBranchCoverageRate));

		coverageResultEntries.add(new CoverageResultEntry(PROJECT, LINE,
				"project", totalLinesCovered / totalLines,
				totalLineCoverageRate));

		return new CoverageThresholdsReport(Collections
				.unmodifiableList(coverageResultEntries));
	}

	private PackageCoverage getPackageCoverage(String packageName) {
		PackageCoverage packageCoverage = packageCoverageMap.get(packageName);
		if (packageCoverage == null) {
			packageCoverage = new PackageCoverage();
			packageCoverageMap.put(packageName, packageCoverage);
		}
		return packageCoverage;
	}

	private List<CoverageResultEntry> checkPackageCoverageLevels(
			double packageBranchCoverageRate, double packageLineCoverageRate) {
		for (Map.Entry<String, PackageCoverage> entry : packageCoverageMap
				.entrySet()) {
			String packageName = entry.getKey();
			PackageCoverage packageCoverage = entry.getValue();

			return checkPackageCoverage(packageBranchCoverageRate,
					packageLineCoverageRate, packageName, packageCoverage);
		}
		return new ArrayList<CoverageResultEntry>();
	}

	private List<CoverageResultEntry> checkPackageCoverage(
			double packageBranchCoverageRate, double packageLineCoverageRate,
			String packageName, PackageCoverage packageCoverage) {
		List<CoverageResultEntry> coverageResultEntries = new ArrayList<CoverageResultEntry>();

		coverageResultEntries.add(new CoverageResultEntry(PACKAGE, BRANCH,
				packageName, packageCoverage.getBranchCoverage()
						/ packageCoverage.getBranchCount(),
				packageBranchCoverageRate));

		coverageResultEntries.add(new CoverageResultEntry(PACKAGE, LINE,
				packageName, packageCoverage.getLineCoverage()
						/ packageCoverage.getLineCount(),
				packageLineCoverageRate));

		return coverageResultEntries;
	}

	private String percentage(double coverateRate) {
		BigDecimal decimal = new BigDecimal(coverateRate * 100);
		return decimal.setScale(1, BigDecimal.ROUND_DOWN).toString();
	}
}
