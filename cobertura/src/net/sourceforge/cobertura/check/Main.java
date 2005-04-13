/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.Header;

import org.apache.log4j.Logger;

public class Main
{

	private static final Logger logger = Logger.getLogger(Main.class);

	Map minimumCoverageRates = new HashMap();
	CoverageRate minimumCoverageRate;

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
		throw new IllegalArgumentException(
				"Invalid value, valid range is [0 .. 100]");
	}

	void setMinimumCoverageRate(String minimumCoverageRate)
	{
		StringTokenizer tokenizer = new StringTokenizer(minimumCoverageRate,
				":");
		minimumCoverageRates.put(Pattern.compile(tokenizer.nextToken()),
				new CoverageRate(inRangeAndDivideByOneHundred(tokenizer
						.nextToken()), inRangeAndDivideByOneHundred(tokenizer
						.nextToken())));
	}

	CoverageRate findMinimumCoverageRate(String classname)
	{
		Iterator i = minimumCoverageRates.entrySet().iterator();
		while (i.hasNext())
		{
			Map.Entry entry = (Map.Entry)i.next();

			Pattern pattern = (Pattern)entry.getKey();
			if (pattern.matcher(classname).matches())
			{
				return (CoverageRate)entry.getValue();
			}
		}
		return minimumCoverageRate;
	}

	public Main(String[] args)
	{
		Header.print(System.out);
		System.out.println("Cobertura coverage check");

		LongOpt[] longOpts = new LongOpt[4];
		longOpts[0] = new LongOpt("branch", LongOpt.REQUIRED_ARGUMENT, null,
				'b');
		longOpts[2] = new LongOpt("datafile", LongOpt.REQUIRED_ARGUMENT,
				null, 'd');
		longOpts[3] = new LongOpt("ignore", LongOpt.REQUIRED_ARGUMENT, null,
				'i');
		longOpts[1] = new LongOpt("line", LongOpt.REQUIRED_ARGUMENT, null,
				'l');

		Getopt g = new Getopt(getClass().getName(), args, ":b:d:i:l:",
				longOpts);
		int c;

		double branchCoverageRate = 0.8;
		double lineCoverageRate = 0.7;

		while ((c = g.getopt()) != -1)
		{
			switch (c)
			{
				case 'b':
					branchCoverageRate = inRangeAndDivideByOneHundred(g
							.getOptarg());
					break;

				case 'd':
					CoverageDataFileHandler.setDefaultDataFile(g.getOptarg());
					break;

				case 'i':
					setMinimumCoverageRate(g.getOptarg());
					break;

				case 'l':
					lineCoverageRate = inRangeAndDivideByOneHundred(g
							.getOptarg());
					break;
			}
		}

		minimumCoverageRate = new CoverageRate(lineCoverageRate,
				branchCoverageRate);

		// Load coverage data
		ProjectData projectData = ProjectData.getGlobalProjectData();

		if (logger.isInfoEnabled())
		{
			logger.info("Coverage data has "
					+ projectData.getNumberOfClasses() + " classes");
		}

		Iterator iter = projectData.getClasses().iterator();
		while (iter.hasNext())
		{
			ClassData classData = (ClassData)iter.next();
			CoverageRate coverageRate = findMinimumCoverageRate(classData
					.getName());

			if (logger.isInfoEnabled())
			{
				StringBuffer sb = new StringBuffer();
				sb.append(classData.getName());
				sb.append(", line: ");
				sb.append(percentage(classData.getLineCoverageRate()));
				sb.append("% (");
				sb.append(percentage(classData.getLineCoverageRate()));
				sb.append("%), branch: ");
				sb.append(percentage(classData.getBranchCoverageRate()));
				sb.append("% (");
				sb.append(percentage(classData.getBranchCoverageRate()));
				sb.append("%)");
				logger.info(sb.toString());
			}

			if (classData.getLineCoverageRate() < coverageRate
					.getLineCoverageRate())
			{
				StringBuffer sb = new StringBuffer();
				sb.append(classData.getName());
				sb.append(" line coverage rate of: ");
				sb.append(percentage(classData.getLineCoverageRate()));
				sb.append("% (required: ");
				sb.append(percentage(coverageRate.getLineCoverageRate()));
				sb.append("%)");
				System.out.println(sb.toString());
			}

			if (classData.getBranchCoverageRate() < coverageRate
					.getBranchCoverageRate())
			{
				StringBuffer sb = new StringBuffer();
				sb.append(classData.getName());
				sb.append(" branch coverage rate of: ");
				sb.append(percentage(classData.getBranchCoverageRate()));
				sb.append("% (required: ");
				sb.append(percentage(coverageRate.getBranchCoverageRate()));
				sb.append("%)");
				System.out.println(sb.toString());
			}
		}
	}

	private String percentage(double coverateRate)
	{
		BigDecimal decimal = new BigDecimal(coverateRate * 100);
		return decimal.setScale(1, BigDecimal.ROUND_DOWN).toString();
	}

	public static void main(String[] args)
	{
		new Main(args);
	}

}
