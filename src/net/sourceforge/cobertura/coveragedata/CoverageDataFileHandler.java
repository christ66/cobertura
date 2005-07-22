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

package net.sourceforge.cobertura.coveragedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * This contains methods used for reading and writing the
 * "cobertura.ser" file.
 */
public abstract class CoverageDataFileHandler implements HasBeenInstrumented
{

	/**
	 * Default file name used to write instrumentation information.
	 */
	public static final String FILE_NAME = "cobertura.ser";

	private static final Logger logger = Logger
			.getLogger(CoverageDataFileHandler.class);

	public static File getDefaultDataFile()
	{
		String systemProperty = System
				.getProperty("net.sourceforge.cobertura.datafile");
		if (systemProperty != null)
			return new File(systemProperty);
		return new File(FILE_NAME);
	}

	public static ProjectData loadCoverageData(File dataFile)
	{
		InputStream is = null;

		logger.info("Loading coverage data from " + dataFile.getAbsolutePath());
		try
		{
			is = new FileInputStream(dataFile);
			return loadCoverageData(is);
		}
		catch (IOException e)
		{
			logger.debug("Error loading file " + dataFile.getAbsolutePath(),
					e);
			return null;
		}
		finally
		{
			if (is != null)
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					logger.debug("Error closing file "
							+ dataFile.getAbsolutePath(), e);
				}
		}
	}

	private static ProjectData loadCoverageData(InputStream dataFile)
	{
		ObjectInputStream objects = null;

		try
		{
			objects = new ObjectInputStream(dataFile);
			ProjectData projectData = (ProjectData)objects.readObject();
			logger.info("Loaded information on "
					+ projectData.getNumberOfClasses() + " classes.");
			return projectData;
		}
		catch (Exception e)
		{
			logger.error("Error reading from object stream.", e);
			return null;
		}
		finally
		{
			if (objects != null)
			{
				try
				{
					objects.close();
				}
				catch (IOException e)
				{
					logger.debug("Error closing object stream.", e);
				}
			}
		}
	}

	public static void saveCoverageData(ProjectData projectData,
			File dataFile)
	{
		FileOutputStream os = null;

		logger.info("Saving coverage data to " + dataFile.getAbsolutePath());
		try
		{
			os = new FileOutputStream(dataFile);
			saveCoverageData(projectData, os);
		}
		catch (IOException e)
		{
			logger.error(e);
		}
		finally
		{
			if (os != null)
			{
				try
				{
					os.close();
				}
				catch (IOException e)
				{
					logger.debug(e);
				}
			}
		}
	}

	private static void saveCoverageData(ProjectData projectData,
			OutputStream dataFile)
	{
		ObjectOutputStream objects = null;

		try
		{
			objects = new ObjectOutputStream(dataFile);
			objects.writeObject(projectData);
			logger.info("Saved information on "
					+ projectData.getNumberOfClasses() + " classes.");
		}
		catch (IOException e)
		{
			logger.error(e);
		}
		finally
		{
			if (objects != null)
			{
				try
				{
					objects.close();
				}
				catch (IOException e)
				{
					logger.debug(e);
				}
			}
		}
	}

}
