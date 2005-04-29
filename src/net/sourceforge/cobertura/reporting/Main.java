/*
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

package net.sourceforge.cobertura.reporting;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.reporting.xml.XMLReport;

import org.apache.log4j.Logger;

public class Main
{

	private static final Logger logger = Logger.getLogger(Main.class);

	// TODO: make these not static?
	static String format = "html";
	static File dataFile;
	static File destinationDir;
	static File sourceDir;

	public static void main(String[] args) throws Exception
	{
		long startTime = System.currentTimeMillis();

		LongOpt[] longOpts = new LongOpt[4];
		// TODO: Allow for multiple destination and multiple source directories
		// TODO: Are format and datafile optional?  They should be
		longOpts[0] = new LongOpt("format", LongOpt.REQUIRED_ARGUMENT, null,
				'f');
		longOpts[1] = new LongOpt("datafile",
				LongOpt.REQUIRED_ARGUMENT, null, 'd');
		longOpts[2] = new LongOpt("destination", LongOpt.REQUIRED_ARGUMENT, null,
				'o');
		longOpts[3] = new LongOpt("source", LongOpt.REQUIRED_ARGUMENT, null,
				's');

		Getopt g = new Getopt(Main.class.getName(), args, ":f:d:o:s:",
				longOpts);

		int c;

		while ((c = g.getopt()) != -1)
		{
			switch (c)
			{
				case 'f':
					format = g.getOptarg();
					if (!format.equalsIgnoreCase("html")
							&& !format.equalsIgnoreCase("xml"))
					{
						System.err.println("Error: format \"" + format
								+ "\" is invalid. Must be either html or xml");
						System.exit(1);
					}
					break;

				case 'd':
					dataFile = new File(g.getOptarg());
					if (!dataFile.exists())
					{
						throw new Exception("Error: data file "
								+ dataFile.getAbsolutePath()
								+ " does not exist");
					}
					if (dataFile.isDirectory())
					{
						throw new Exception("Error: data file "
								+ dataFile.getAbsolutePath()
								+ " cannot be a directory");
					}
					break;

				case 'o':
					destinationDir = new File(g.getOptarg());
					if (destinationDir.exists() && destinationDir.isFile())
					{
						throw new Exception("Error: destination directory "
								+ destinationDir + " already exists and is a file");
					}
					destinationDir.mkdirs();
					break;

				case 's':
					sourceDir = new File(g.getOptarg());
					if (!sourceDir.exists())
					{
						throw new Exception("Error: source directory "
								+ sourceDir + " does not exist");
					}
					if (sourceDir.isFile())
					{
						throw new Exception("Error: source directory "
								+ sourceDir
								+ " should be a directory, not a file");
					}
					break;
			}
		}

		if (logger.isDebugEnabled())
		{
			logger.debug("format is " + format);
			logger.debug("dataFile is "
					+ dataFile.getAbsolutePath());
			logger.debug("destinationDir is " + destinationDir.getAbsolutePath());
			logger.debug("sourceDir is " + sourceDir.getAbsolutePath());
		}

		if (dataFile == null)
			dataFile = CoverageDataFileHandler.getDefaultDataFile();
		ProjectData projectData = CoverageDataFileHandler
				.loadCoverageData(dataFile);

		if (format.equalsIgnoreCase("html"))
		{
			new HTMLReport(projectData, destinationDir, sourceDir);
		}
		else if (format.equalsIgnoreCase("xml"))
		{
			new XMLReport(projectData, destinationDir, sourceDir);
		}

		long stopTime = System.currentTimeMillis();
		System.out
				.println("Reporting time: " + (stopTime - startTime) + "ms");
	}

}