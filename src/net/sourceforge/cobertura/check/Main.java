/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import net.sourceforge.cobertura.coverage.CoverageData;
import net.sourceforge.cobertura.coverage.InstrumentationPersistence;
import net.sourceforge.cobertura.util.Copyright;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class Main extends InstrumentationPersistence
{

	private static final Logger logger = Logger.getLogger(Main.class);

	final Perl5Matcher pm = new Perl5Matcher();
	final Perl5Compiler pc = new Perl5Compiler();

	Map minimumCoverageRates = new HashMap();
	CoverageRate minimumCoverageRate;

	File instrumentationDirectory = new File(System.getProperty("user.dir"));

	void setInstrumentationDirectory(File instrumentationDirectory)
	{
		this.instrumentationDirectory = instrumentationDirectory;
	}

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
			throws MalformedPatternException
	{
		StringTokenizer tokenizer = new StringTokenizer(minimumCoverageRate,
				":");
		minimumCoverageRates.put(pc.compile(tokenizer.nextToken()),
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

			if (pm.matches(classname, (Pattern)entry.getKey()))
			{
				return (CoverageRate)entry.getValue();
			}
		}
		return minimumCoverageRate;
	}

	public Main(String[] args) throws IOException, MalformedPatternException
	{
		Copyright.print(System.out);
		System.out.println("Cobertura coverage check");

		LongOpt[] longOpts = new LongOpt[4];
		longOpts[0] = new LongOpt("branch", LongOpt.REQUIRED_ARGUMENT, null,
				'b');
		longOpts[1] = new LongOpt("line", LongOpt.REQUIRED_ARGUMENT, null,
				'l');
		longOpts[2] = new LongOpt("directory", LongOpt.REQUIRED_ARGUMENT,
				null, 'd');
		longOpts[3] = new LongOpt("regex", LongOpt.REQUIRED_ARGUMENT, null,
				'r');

		Getopt g = new Getopt(getClass().getName(), args, ":b:l:d:r:",
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

				case 'l':
					lineCoverageRate = inRangeAndDivideByOneHundred(g
							.getOptarg());
					break;

				case 'd':
					setInstrumentationDirectory(new File(g.getOptarg()));
					break;

				case 'r':
					setMinimumCoverageRate(g.getOptarg());
					break;
			}
		}

		minimumCoverageRate = new CoverageRate(lineCoverageRate,
				branchCoverageRate);

		if (logger.isInfoEnabled())
		{
			logger.info("instrumentation directory: "
					+ instrumentationDirectory);
		}

		merge(loadInstrumentation(new FileInputStream(new File(
				instrumentationDirectory, CoverageData.FILE_NAME))));

		if (logger.isInfoEnabled())
		{
			logger
					.info("instrumentation has " + keySet().size()
							+ " entries");
		}

		Iterator i = keySet().iterator();
		while (i.hasNext())
		{
			String key = (String)i.next();

			CoverageRate coverageRate = findMinimumCoverageRate(key);
			CoverageData instrumentation = getInstrumentation(key);

			if (logger.isInfoEnabled())
			{
				StringBuffer sb = new StringBuffer();
				sb.append(key);
				sb.append(", line: ");
				sb.append(percentage(instrumentation.getLineCoverageRate()));
				sb.append("% (");
				sb.append(percentage(coverageRate.getLineCoverageRate()));
				sb.append("%), branch: ");
				sb
						.append(percentage(instrumentation
								.getBranchCoverageRate()));
				sb.append("% (");
				sb.append(percentage(coverageRate.getBranchCoverageRate()));
				sb.append("%)");
				logger.info(sb.toString());
			}

			if (instrumentation.getLineCoverageRate() < coverageRate
					.getLineCoverageRate())
			{
				StringBuffer sb = new StringBuffer();
				sb.append(key);
				sb.append(" line coverage rate of: ");
				sb.append(percentage(instrumentation.getLineCoverageRate()));
				sb.append("% (required: ");
				sb.append(percentage(coverageRate.getLineCoverageRate()));
				sb.append("%)");
				System.out.println(sb.toString());
			}

			if (instrumentation.getBranchCoverageRate() < coverageRate
					.getBranchCoverageRate())
			{
				StringBuffer sb = new StringBuffer();
				sb.append(key);
				sb.append(" branch coverage rate of: ");
				sb
						.append(percentage(instrumentation
								.getBranchCoverageRate()));
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

	public static void main(String[] args) throws IOException,
			MalformedPatternException
	{
		new Main(args);
	}
}