/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Nathan Wilson
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

package net.sourceforge.cobertura.check;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.Header;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class Main
{

	private static final Logger logger = Logger.getLogger(Main.class);

	final Perl5Matcher pm = new Perl5Matcher();

	final Perl5Compiler pc = new Perl5Compiler();

	/**
	 * The default CoverageRate needed for a class to pass the check.
	 */
	CoverageRate minimumCoverageRate;

	/**
	 * The keys of this map contain regular expression Patterns that
	 * match against classes.  The values of this map contain
	 * CoverageRate objects that specify the minimum coverage rates
	 * needed for a class that matches the pattern.
	 */
	Map minimumCoverageRates = new HashMap();

	double inRangeAndDivideByOneHundred(String coverageRateAsPercentage)
	{
		return inRangeAndDivideByOneHundred(Integer.valueOf(
				coverageRateAsPercentage).intValue());
	}

	double inRangeAndDivideByOneHundred(int coverageRateAsPercentage)
	{
		if ((coverageRateAsPercentage >= 0)
				&& (coverageRateAsPercentage <= 100))
		{
			return (double)coverageRateAsPercentage / 100;
		}
		throw new IllegalArgumentException("The value "
				+ coverageRateAsPercentage
				+ "% is invalid.  Percentages must be between 0 and 100.");
	}

	void setMinimumCoverageRate(String minimumCoverageRate)
			throws MalformedPatternException
	{
		StringTokenizer tokenizer = new StringTokenizer(minimumCoverageRate,
				":");
		this.minimumCoverageRates.put(pc.compile(tokenizer.nextToken()),
				new CoverageRate(inRangeAndDivideByOneHundred(tokenizer
						.nextToken()), inRangeAndDivideByOneHundred(tokenizer
						.nextToken())));
	}

	/**
	 * This method returns the CoverageRate object that
	 * applies to the given class.  If checks if there is a
	 * pattern that matches the class name, and returns that
	 * if it finds one.  Otherwise it uses the global minimum
	 * rates that were passed in.
	 */
	CoverageRate findMinimumCoverageRate(String classname)
	{
		Iterator iter = this.minimumCoverageRates.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry)iter.next();

			if (pm.matches(classname, (Pattern)entry.getKey()))
			{
				return (CoverageRate)entry.getValue();
			}
		}
		return this.minimumCoverageRate;
	}

	public Main(String[] args) throws MalformedPatternException
	{
		int exitStatus = 0;

		Header.print(System.out);

		File dataFile = CoverageDataFileHandler.getDefaultDataFile();
		double branchCoverageRate = 0.0;
		double lineCoverageRate = 0.0;
		double totalBranchCoverageRate = 0.0;
		double totalLineCoverageRate = 0.0;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--branch"))
			{
				branchCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			}
			else if (args[i].equals("--datafile"))
			{
				dataFile = new File(args[++i]);
			}
			else if (args[i].equals("--line"))
			{
				lineCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			}
			else if (args[i].equals("--regex"))
			{
				setMinimumCoverageRate(args[++i]);
			}
			else if (args[i].equals("--totalbranch"))
			{
				totalBranchCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			}
			else if (args[i].equals("--totalline"))
			{
				totalLineCoverageRate = inRangeAndDivideByOneHundred(args[++i]);
			}
		}

		ProjectData projectData = CoverageDataFileHandler
				.loadCoverageData(dataFile);

		if (projectData == null)
		{
			System.err.println("Error: Unable to read from data file "
					+ dataFile.getAbsolutePath());
			System.exit(1);
		}

		// If they didn't specify any thresholds, then use some defaults
		if ((branchCoverageRate == 0) && (lineCoverageRate == 0)
				&& (totalLineCoverageRate == 0)
				&& (totalBranchCoverageRate == 0)
				&& (this.minimumCoverageRates.size() == 0))
		{
			branchCoverageRate = 0.5;
			lineCoverageRate = 0.5;
			totalBranchCoverageRate = 0.5;
			totalLineCoverageRate = 0.5;
		}

		this.minimumCoverageRate = new CoverageRate(lineCoverageRate,
				branchCoverageRate);

		double totalLines = 0;
		double totalLinesCovered = 0;
		double totalBranches = 0;
		double totalBranchesCovered = 0;

		Iterator iter = projectData.getClasses().iterator();
		while (iter.hasNext())
		{
			ClassData classData = (ClassData)iter.next();
			CoverageRate coverageRate = findMinimumCoverageRate(classData
					.getName());

			if (totalBranchCoverageRate > 0.0)
			{
				totalBranches += classData.getNumberOfValidBranches();
				totalBranchesCovered += classData.getNumberOfCoveredBranches();
			}

			if (totalLineCoverageRate > 0.0)
			{
				totalLines += classData.getNumberOfValidLines();
				totalLinesCovered += classData.getNumberOfCoveredLines();
			}

			logger.debug("Class " + classData.getName()
					+ ", line coverage rate: "
					+ percentage(classData.getLineCoverageRate())
					+ "%, branch coverage rate: "
					+ percentage(classData.getBranchCoverageRate()) + "%");

			if (classData.getBranchCoverageRate() < coverageRate
					.getBranchCoverageRate())
			{
				System.err.println(classData.getName()
						+ " failed check. Branch coverage rate of "
						+ percentage(classData.getBranchCoverageRate())
						+ "% is below "
						+ percentage(coverageRate.getBranchCoverageRate())
						+ "%");
				exitStatus |= 2;
			}

			if (classData.getLineCoverageRate() < coverageRate
					.getLineCoverageRate())
			{
				System.err.println(classData.getName()
						+ " failed check. Line coverage rate of "
						+ percentage(classData.getLineCoverageRate())
						+ "% is below "
						+ percentage(coverageRate.getLineCoverageRate()) + "%");
				exitStatus |= 4;
			}
		}

		// Check the rates for the overal project
		if ((totalBranches > 0)
				&& (totalBranchCoverageRate > (totalBranchesCovered / totalBranches)))
		{
			System.err
					.println("Project failed check. "
							+ "Total branch coverage rate of "
							+ percentage(totalBranchesCovered / totalBranches)
							+ "% is below "
							+ percentage(totalBranchCoverageRate) + "%");
			exitStatus |= 8;
		}

		if ((totalLines > 0)
				&& (totalLineCoverageRate > (totalLinesCovered / totalLines)))
		{
			System.err.println("Project failed check. "
					+ "Total line coverage rate of "
					+ percentage(totalLinesCovered / totalLines)
					+ "% is below " + percentage(totalLineCoverageRate) + "%");
			exitStatus |= 16;
		}

		System.exit(exitStatus);
	}

	private String percentage(double coverateRate)
	{
		BigDecimal decimal = new BigDecimal(coverateRate * 100);
		return decimal.setScale(1, BigDecimal.ROUND_DOWN).toString();
	}

	public static void main(String[] args) throws MalformedPatternException
	{
		new Main(args);
	}

}
