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

package net.sourceforge.cobertura.reporting;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.reporting.xml.XMLReport;

import org.apache.log4j.Logger;

public class Main
{

	private static final Logger logger = Logger.getLogger(Main.class);

	// TODO: make these not static?
	static String format;
	static File serializationFile;
	static File sourceDir;
	static File outputDir;

	public static void main(String[] args) throws Exception
	{
		long startTime = System.currentTimeMillis();

		LongOpt[] longOpts = new LongOpt[4];
		longOpts[0] = new LongOpt("format", LongOpt.REQUIRED_ARGUMENT, null,
				'f');
		longOpts[1] = new LongOpt("instrumentation",
				LongOpt.REQUIRED_ARGUMENT, null, 'i');
		longOpts[2] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null,
				'o');
		longOpts[3] = new LongOpt("source", LongOpt.REQUIRED_ARGUMENT, null,
				's');

		Getopt g = new Getopt(Main.class.getName(), args, ":f:i:o:s:",
				longOpts);

		int c;

		while ((c = g.getopt()) != -1)
		{
			switch (c)
			{
				case 'f':
					format = new String(g.getOptarg());
					if (!format.equalsIgnoreCase("html")
							&& !format.equalsIgnoreCase("xml"))
					{
						throw new Exception("Error: format \"" + format
								+ "\" must be either html or xml");
					}
					break;

				case 'i':
					serializationFile = new File(g.getOptarg());
					if (!serializationFile.exists())
					{
						throw new Exception("Error: serialization file "
								+ serializationFile + " does not exist");
					}
					if (serializationFile.isDirectory())
					{
						throw new Exception("Error: serialization file "
								+ serializationFile
								+ " cannot be a directory");
					}
					break;

				case 'o':
					outputDir = new File(g.getOptarg());
					if (outputDir.exists() && outputDir.isFile())
					{
						throw new Exception("Error: destination directory "
								+ outputDir + " already exists and is a file");
					}
					outputDir.mkdirs();
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
			logger.debug("serializationFile is "
					+ serializationFile.getAbsolutePath());
			logger.debug("outputDir is " + outputDir.getAbsolutePath());
			logger.debug("sourceDir is " + sourceDir.getAbsolutePath());
		}

		InputStream is = null;
		ObjectInputStream objects = null;
		try
		{
			is = new FileInputStream(serializationFile);
			objects = new ObjectInputStream(is);
			Map coverageData = (Map)objects.readObject();

			if (format.equalsIgnoreCase("xml"))
			{
				new XMLReport(coverageData, outputDir, sourceDir);
			}
			else if (format.equalsIgnoreCase("html"))
			{
				new HTMLReport(coverageData, outputDir, sourceDir);
			}
		}
		finally
		{
			if (is != null)
				is.close();
			if (objects != null)
				objects.close();
		}

		long stopTime = System.currentTimeMillis();
		System.out
				.println("Reporting time: " + (stopTime - startTime) + "ms");
	}

}