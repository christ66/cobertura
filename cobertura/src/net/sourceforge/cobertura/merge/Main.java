/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Mark Sinke
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

package net.sourceforge.cobertura.merge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.Header;

import org.apache.log4j.Logger;

public class Main
{

	private static final Logger logger = Logger.getLogger(Main.class);

	public Main(String[] args)
	{
		File dataFile = CoverageDataFileHandler.getDefaultDataFile();
		ProjectData projectData = null;

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--datafile"))
			{
				File newDataFile = new File(args[++i]);
				if (projectData == null) {
					projectData = CoverageDataFileHandler
							.loadCoverageData(newDataFile);
				} else {
					ProjectData projectDataNew = CoverageDataFileHandler
					.loadCoverageData(newDataFile);
					projectData.merge(projectDataNew);
				}
			}
			else if (args[i].equals("--output"))
			{
				dataFile = new File(args[++i]);
				dataFile.getParentFile().mkdirs();
			}
		}

		if (projectData != null)
			CoverageDataFileHandler.saveCoverageData(projectData, dataFile);
	}

	public static void main(String[] args)
	{
		Header.print(System.out);

		boolean hasCommandsFile = false;
		String commandsFileName = null;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--commandsfile"))
			{
				hasCommandsFile = true;
				commandsFileName = args[++i];
			}
		}

		if (hasCommandsFile)
		{
			List arglist = new ArrayList();
			BufferedReader bufreader = null;

			try
			{
				bufreader = new BufferedReader(new FileReader(
						commandsFileName));
				String line;

				while ((line = bufreader.readLine()) != null)
				{
					arglist.add(line);
				}

			}
			catch (IOException e)
			{
				logger.fatal("Unable to read temporary commands file "
						+ commandsFileName + ".");
				logger.info(e);
			}
			finally
			{
				if (bufreader != null)
				{
					try
					{
						bufreader.close();
					}
					catch (IOException e)
					{
					}
				}
			}
			args = (String[])arglist.toArray(new String[arglist.size()]);
		}

		new Main(args);
	}
}
