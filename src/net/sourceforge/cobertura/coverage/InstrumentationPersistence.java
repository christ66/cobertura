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

package net.sourceforge.cobertura.coverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class InstrumentationPersistence implements HasBeenInstrumented
{

	/**
	 * Default file name used to write instrumentation information.
	 */
	public static final String FILE_NAME = "cobertura.ser";

	private static final Logger logger = Logger
			.getLogger(InstrumentationPersistence.class);

	final Map instrumentation = new HashMap();

	protected Map loadInstrumentation()
	{
		File directory = getDirectory();

		if (logger.isInfoEnabled())
		{
			logger.info("loading: " + directory + '/' + FILE_NAME);
		}

		try
		{
			return loadInstrumentation(new FileInputStream(new File(
					directory, FILE_NAME)));
		}
		catch (FileNotFoundException ex)
		{
			logger.info(ex);
			return Collections.EMPTY_MAP;
		}
	}

	protected Map loadInstrumentation(InputStream is)
	{
		ObjectInputStream objects = null;
		try
		{
			objects = new ObjectInputStream(is);
			Map m = (Map)objects.readObject();
			if (logger.isInfoEnabled())
			{
				logger.info("loaded " + m.size() + " entries.");
			}
			return m;
		}
		catch (ClassNotFoundException ex)
		{
			logger.error(ex);
			return Collections.EMPTY_MAP;
		}
		catch (IOException ex)
		{
			logger.error(ex);
			return Collections.EMPTY_MAP;
		}
		finally
		{
			if (objects != null)
			{
				try
				{
					objects.close();
				}
				catch (IOException ex)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug(ex);
					}
				}
			}

			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException ex)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug(ex);
					}
				}
			}
		}
	}

	protected void merge(Map m)
	{
		Iterator i = m.entrySet().iterator();
		while (i.hasNext())
		{
			Map.Entry entry = (Map.Entry)i.next();
			String classname = (String)entry.getKey();
			if (instrumentation.containsKey(classname))
			{
				getInstrumentation(classname).merge(
						(CoverageData)entry.getValue());
			}
			else
			{
				instrumentation.put(classname, entry.getValue());
			}
		}
	}

	private File getDirectory()
	{
		if (System.getProperty("net.sourceforge.cobertura.rawcoverage.dir") != null)
		{
			return new File(System
					.getProperty("net.sourceforge.cobertura.rawcoverage.dir"));
		}
		return new File(System.getProperty("user.dir"));
	}

	protected void saveInstrumentation()
	{
		File directory = getDirectory();

		if (logger.isInfoEnabled())
		{
			logger.info("saving: " + directory + '/' + FILE_NAME);
		}

		saveInstrumentation(directory);
	}

	protected void saveInstrumentation(File destDir)
	{
		FileOutputStream os = null;
		ObjectOutputStream objects = null;

		try
		{
			os = new FileOutputStream(new File(destDir, FILE_NAME));
			objects = new ObjectOutputStream(os);
			objects.writeObject(instrumentation);
			if (logger.isInfoEnabled())
			{
				logger.info("saved " + instrumentation.size() + " entries.");
			}
		}
		catch (IOException ex)
		{
			logger.error(ex);
		}
		finally
		{
			if (objects != null)
			{
				try
				{
					objects.close();
				}
				catch (IOException ex)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug(ex);
					}
				}
			}

			if (os != null)
			{
				try
				{
					os.close();
				}
				catch (IOException ex)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug(ex);
					}
				}
			}
		}
	}

	protected CoverageData getInstrumentation(String classname)
	{
		return (CoverageData)instrumentation.get(classname);
	}

	protected Set keySet()
	{
		return Collections.unmodifiableSet(instrumentation.keySet());
	}

}