/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
 * Copyright (C) 2005 Joakim Erdfelt <joakim@erdfelt.net>
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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.cobertura.coverage.InstrumentationPersistence;
import net.sourceforge.cobertura.util.Copyright;

import org.apache.log4j.Logger;

public class Main extends InstrumentationPersistence
{

	private static final Logger logger = Logger.getLogger(Main.class);

	public Main(String[] args)
	{
		LongOpt[] longOpts = new LongOpt[2];
		longOpts[0] = new LongOpt("instrumentation",
				LongOpt.REQUIRED_ARGUMENT, null, 'i');
		longOpts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null,
				'o');

		Getopt g = new Getopt(getClass().getName(), args, ":i:o:", longOpts);
		int c;

		File destDir = new File(System.getProperty("user.dir"));

		while ((c = g.getopt()) != -1)
		{
			switch (c)
			{
				case 'i':
					System.out.println("cobertura loading: " + g.getOptarg());
					try
					{
						merge(loadInstrumentation(new FileInputStream(g
								.getOptarg())));
					}
					catch (FileNotFoundException ex)
					{
						logger.warn(ex);
					}
					break;

				case 'o':
					destDir = new File(g.getOptarg());
					destDir.mkdirs();
					break;
			}
		}

		saveInstrumentation(destDir);
	}

	public static void main(String[] args)
	{
		Copyright.print(System.out);
		System.out.println("Cobertura instrumentation merge tool");

		boolean hasCommandsFile = false;
		String commandsFileName = null;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-commandsfile"))
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
