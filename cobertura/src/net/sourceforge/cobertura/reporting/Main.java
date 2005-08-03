/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Jeremy Thomerson
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.reporting.xml.XMLReport;
import net.sourceforge.cobertura.util.FileFinder;

import org.apache.log4j.Logger;

public class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	private String format = "html";
	private File dataFile = null;
	private File destinationDir = null;
	
	private void parseArguments(String[] args) throws Exception {
		FileFinder finder = new FileFinder();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--basedir")) {
				finder.addBaseDirectory(new File(args[++i]));
			} else if (args[i].equals("--datafile")) {
				dataFile = new File(args[++i]);
			} else if (args[i].equals("--destination")) {
				destinationDir = new File(args[++i]);
				destinationDir.mkdirs();
			} else if (args[i].equals("--format")) {
				format = args[++i];
				checkFormat();
			} else {
				finder.addSourceFilePath(args[i]);
			}
		}

		ProjectData projectData = CoverageDataFileHandler.loadCoverageData(dataFile);

		if (projectData == null) {
			System.err.println("Error: Unable to read from data file " + dataFile.getAbsolutePath());
			System.exit(1);
		}

		if (format.equalsIgnoreCase("html")) {
			new HTMLReport(projectData, destinationDir, finder);
		} else if (format.equalsIgnoreCase("xml")) {
			new XMLReport(projectData, destinationDir, finder);
		}
	}
	
	private void checkFormat() {
		if (!format.equalsIgnoreCase("html") && !format.equalsIgnoreCase("xml")) {
			System.err.println("" +
					"Error: format \"" +
					format + "\" is invalid. Must be either html or xml"
					);
			System.exit(1);
		}
	}
	
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

		Main main = new Main();

		boolean hasCommandsFile = false;
		String commandsFileName = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--commandsfile")) {
				hasCommandsFile = true;
				commandsFileName = args[++i];
			}
		}

		if (hasCommandsFile) {
			List arglist = new ArrayList();
			BufferedReader bufferedReader = null;

			try {
				bufferedReader = new BufferedReader(new FileReader(commandsFileName));
				String line = null;

				while ((line = bufferedReader.readLine()) != null) {
					arglist.add(line);
				}
			} catch (IOException e) {
				LOGGER.fatal("Unable to read temporary commands file " + commandsFileName + ".", e);
			} finally {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (IOException e) {
						// no-op
					}
				}
			}

			args = (String[])arglist.toArray(new String[arglist.size()]);
		}

		main.parseArguments(args);

		long stopTime = System.currentTimeMillis();
		System.out.println("Report time: " + (stopTime - startTime) + "ms");
	}

}
