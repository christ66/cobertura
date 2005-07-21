/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Grzegorz Lukasik
 * Contact information for the above is given in the COPYRIGHT file.
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

package net.sourceforge.cobertura.instrument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.IOUtil;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * <p>
 * Add coverage instrumentation to existing classes.
 * </p>
 *
 * <h3>What does that mean, exactly?</h3>
 * <p>
 * It means Cobertura will look at each class you give it.  It
 * loads the bytecode into memory.  For each line of source,
 * Cobertura adds a few extra instructions.  These instructions 
 * do the following:
 * </p>
 * 
 * <ol>
 * <li>Get an instance of the ProjectData class.</li>
 * <li>Call a method in this ProjectData class that increments
 * a counter for this line of code.
 * </ol>
 *
 * <p>
 * After every line in a class has been "instrumented," Cobertura
 * edits the bytecode for the class one more time and adds
 * "implements net.sourceforge.cobertura.coveragedata.HasBeenInstrumented" 
 * This is basically just a flag used internally by Cobertura to
 * determine whether a class has been instrumented or not, so
 * as not to instrument the same class twice.
 * </p>
 */
public class Main
{

	private static final Logger logger = Logger.getLogger(Main.class);

	private File destinationDirectory = null;

	private File baseDir = null;

	private Pattern ignoreRegexp = null;

	private ProjectData projectData = null;

	/**
	 * @param file A file.
	 * @return True if the specified file has "class" as its extension,
	 * false otherwise.
	 */
	private static boolean isClass(File file)
	{
		return file.getName().endsWith(".class");
	}

	/**
	 * @param entry A zip entry.
	 * @return True if the specified entry has "class" as its extension,
	 * false otherwise.
	 */
	private static boolean isClass(ZipEntry entry)
	{
		return entry.getName().endsWith(".class");
	}

	/**
	 * @return True if file has an extension that matches one of the
	 *         standard java archives, false otherwise.
	 */
	private static boolean isArchive(File file)
	{
		String name = file.getName();
		return name.endsWith(".jar") || name.endsWith(".zip")
				|| name.endsWith(".war") || name.endsWith(".ear")
				|| name.endsWith(".sar");
	}

	private void addInstrumentationToArchive(ZipInputStream archive,
			ZipOutputStream output) throws Exception
	{
		ZipEntry entry;
		while ((entry = archive.getNextEntry()) != null)
		{
			try
			{
				ZipEntry outputEntry = new ZipEntry(entry.getName());
				output.putNextEntry(outputEntry);

				// Read current entry
				byte[] entryBytes = IOUtil
						.createByteArrayFromInputStream(archive);

				// Check if we have class file
				if (isClass(entry))
				{
					// Instrument class
					ClassReader cr = new ClassReader(entryBytes);
					ClassWriter cw = new ClassWriter(true);
					ClassInstrumenter cv = new ClassInstrumenter(projectData,
							cw, ignoreRegexp);
					cr.accept(cv, false);

					// If class was instrumented, get bytes that define the
					// class
					if (cv.isInstrumented())
					{
						logger.debug("Putting instrumeted entry: "
								+ entry.getName());
						entryBytes = cw.toByteArray();
					}
				}

				// Add entry to the output
				output.write(entryBytes);
				output.closeEntry();
				archive.closeEntry();
			}
			catch (Exception e)
			{
				logger.warn("Problems with archive entry: " + entry);
				throw e;
			}
			output.flush();
		}
	}

	private void addInstrumentationToArchive(File archive)
	{
		logger.debug("Instrumenting archive " + archive.getAbsolutePath());

		File outputFile = null;
		ZipInputStream input = null;
		ZipOutputStream output = null;
		try
		{
			// Open archive
			try
			{
				input = new ZipInputStream(new FileInputStream(archive));
			}
			catch (FileNotFoundException e)
			{
				logger.warn("Cannot open archive file: "
						+ archive.getAbsolutePath(), e);
				return;
			}

			// Open output archive
			try
			{
				// check if destination folder is set
				if (destinationDirectory != null)
				{
					// if so, create output file in it
					outputFile = new File(destinationDirectory, archive
							.getName());
				}
				else
				{
					// otherwise create output file in temporary location
					outputFile = File.createTempFile(
							"CoberturaInstrumentedArchive", "jar");
					outputFile.deleteOnExit();
				}
				output = new ZipOutputStream(new FileOutputStream(outputFile));
			}
			catch (IOException e)
			{
				logger.warn("Cannot open file for instrumented archive: "
						+ archive.getAbsolutePath(), e);
				return;
			}

			// Instrument classes in archive
			try
			{
				addInstrumentationToArchive(input, output);
			}
			catch (Exception e)
			{
				logger.warn("Cannot instrument archive: "
						+ archive.getAbsolutePath(), e);
				return;
			}
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (IOException e)
				{
				}
			}
			if (output != null)
			{
				try
				{
					output.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		// If destination folder was not set, overwrite orginal archive with
		// instrumented one
		if (destinationDirectory == null)
		{
			try
			{
				IOUtil.moveFile(outputFile, archive);
			}
			catch (IOException e)
			{
				logger.warn("Cannot instrument archive: "
						+ archive.getAbsolutePath(), e);
				return;
			}
		}
	}

	private void addInstrumentationToSingleClass(File file)
	{
		logger.debug("Instrumenting class " + file.getAbsolutePath());

		InputStream inputStream = null;
		ClassWriter cw;
		ClassInstrumenter cv;
		try
		{
			inputStream = new FileInputStream(file);
			ClassReader cr = new ClassReader(inputStream);
			cw = new ClassWriter(true);
			cv = new ClassInstrumenter(projectData, cw, ignoreRegexp);
			cr.accept(cv, false);
		}
		catch (Throwable t)
		{
			logger.warn("Unable to instrument file " + file.getAbsolutePath(),
					t);
			return;
		}
		finally
		{
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		OutputStream outputStream = null;
		try
		{
			if (cv.isInstrumented())
			{
				// If destinationDirectory is null, then overwrite
				// the original, uninstrumented file.
				File outputFile;
				if (destinationDirectory == null)
					outputFile = file;
				else
					outputFile = new File(destinationDirectory, cv
							.getClassName().replace('.', File.separatorChar)
							+ ".class");

				File parentFile = outputFile.getParentFile();
				if (parentFile != null)
				{
					parentFile.mkdirs();
				}

				byte[] instrumentedClass = cw.toByteArray();
				outputStream = new FileOutputStream(outputFile);
				outputStream.write(instrumentedClass);
			}
		}
		catch (IOException e)
		{
			logger.warn("Unable to instrument file " + file.getAbsolutePath(),
					e);
			return;
		}
		finally
		{
			if (outputStream != null)
			{
				try
				{
					outputStream.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	// TODO: Don't attempt to instrument a file if the outputFile already
	//       exists and is newer than the input file.
	private void addInstrumentation(File file)
	{
		if (isClass(file))
		{
			addInstrumentationToSingleClass(file);
		}
		else if (file.isDirectory())
		{
			File[] contents = file.listFiles();
			for (int i = 0; i < contents.length; i++)
				addInstrumentation(contents[i]);
		}
		else if (isArchive(file))
		{
			addInstrumentationToArchive(file);
		}
	}

	private void addInstrumentation(String filename)
	{
		logger.debug("filename: " + filename);

		File file;
		if (baseDir == null)
			file = new File(filename);
		else
			file = new File(baseDir, filename);

		addInstrumentation(file);
	}

	private void parseArguments(String[] args)
	{
		File dataFile = CoverageDataFileHandler.getDefaultDataFile();

		// Parse our parameters
		Collection locations = new Vector();
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--basedir"))
				baseDir = new File(args[++i]);
			else if (args[i].equals("--datafile"))
				dataFile = new File(args[++i]);
			else if (args[i].equals("--destination"))
				destinationDirectory = new File(args[++i]);
			else if (args[i].equals("--ignore"))
			{
				String regex = args[++i];
				try
				{
					Perl5Compiler pc = new Perl5Compiler();
					this.ignoreRegexp = pc.compile(regex);
				}
				catch (MalformedPatternException e)
				{
					logger.warn("The regular expression " + regex
							+ " is invalid: " + e.getLocalizedMessage());
				}
			}
			else
				locations.add(args[i]);
		}

		// Load coverage data, instrument classes, save coverage data
		projectData = CoverageDataFileHandler.loadCoverageData(dataFile);
		if (projectData == null)
			projectData = new ProjectData();
		Iterator iter = locations.iterator();
		while (iter.hasNext())
			addInstrumentation((String)iter.next());
		CoverageDataFileHandler.saveCoverageData(projectData, dataFile);
	}

	public static void main(String[] args)
	{
		long startTime = System.currentTimeMillis();

		Main main = new Main();

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
			BufferedReader bufferedReader = null;

			try
			{
				bufferedReader = new BufferedReader(new FileReader(
						commandsFileName));
				String line;

				while ((line = bufferedReader.readLine()) != null)
					arglist.add(line);

			}
			catch (IOException e)
			{
				logger.fatal("Unable to read temporary commands file "
						+ commandsFileName + ".");
				logger.info(e);
			}
			finally
			{
				if (bufferedReader != null)
				{
					try
					{
						bufferedReader.close();
					}
					catch (IOException e)
					{
					}
				}
			}

			args = (String[])arglist.toArray(new String[arglist.size()]);
		}

		main.parseArguments(args);

		long stopTime = System.currentTimeMillis();
		System.out.println("Instrument time: " + (stopTime - startTime) + "ms");
	}

}
