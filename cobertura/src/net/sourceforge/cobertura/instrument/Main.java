/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2006 John Lewis
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.CommandLineBuilder;
import net.sourceforge.cobertura.util.Header;
import net.sourceforge.cobertura.util.IOUtil;
import net.sourceforge.cobertura.util.RegexUtil;

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

	private Collection ignoreRegexes = new Vector();

	private Collection includeClassesRegexes = new HashSet();

	private Collection excludeClassesRegexes = new HashSet();

	private ProjectData projectData = null;

	/**
	 * @param entry A zip entry.
	 * @return True if the specified entry has "class" as its extension,
	 * false otherwise.
	 */
	private static boolean isClass(ZipEntry entry)
	{
		return entry.getName().endsWith(".class");
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
				if (isClass(entry) && shouldInstrument(entry.getName()))
				{
					// Instrument class
					ClassReader cr = new ClassReader(entryBytes);
					ClassWriter cw = new ClassWriter(true);
					ClassInstrumenter cv = new ClassInstrumenter(projectData,
							cw, ignoreRegexes);
					cr.accept(cv, CustomAttribute.getExtraAttributes(), false);

					// If class was instrumented, get bytes that define the
					// class
					if (cv.isInstrumented())
					{
						logger.debug("Putting instrumented entry: "
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

	private boolean shouldInstrument(String name)
	{
		boolean shouldInstrument = true;

		if (includeClassesRegexes.size() > 0)
		{
			shouldInstrument = false;
			// Remove .class extension if it exists
			if (name.endsWith(".class")) {
				name = name.substring(0, name.length() - 6);
			}
			name = name.replace('/', '.');
			name = name.replace('\\', '.');
			if (RegexUtil.matches(includeClassesRegexes, name)) {
				shouldInstrument = true;
			}
			if (shouldInstrument && RegexUtil.matches(excludeClassesRegexes, name)) {
				shouldInstrument = false;
			}
		}
		return shouldInstrument;
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
			cv = new ClassInstrumenter(projectData, cw, ignoreRegexes);
			cr.accept(cv, CustomAttribute.getExtraAttributes(), false);
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
		catch (Throwable t)
		{
			logger.warn("Unable to instrument file " + file.getAbsolutePath(),
					t);
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
	//       exists and is newer than the input file, and the output and
	//       input file are in different locations?
	private void addInstrumentation(FileInfo fileInfo)
	{
		if (fileInfo.isClass() && shouldInstrument(fileInfo.pathname))
		{
			addInstrumentationToSingleClass(fileInfo);
		}
		else if (fileInfo.isDirectory())
		{
			String[] contents = fileInfo.list();
			for (int i = 0; i < contents.length; i++)
			{
				File relativeFile = new File(fileInfo.pathname, contents[i]);
				FileInfo relativeFileInfo = new FileInfo(fileInfo.baseDir, relativeFile.toString());
				//recursion!
				addInstrumentation(relativeFileInfo);
			}
		}
	}

	private void parseArguments(String[] args)
	{
		File dataFile = CoverageDataFileHandler.getDefaultDataFile();

		// Parse our parameters
		List filePaths = new ArrayList();
		String baseDir = null;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--basedir"))
				baseDir = args[++i];
			else if (args[i].equals("--datafile"))
				dataFile = new File(args[++i]);
			else if (args[i].equals("--destination"))
				destinationDirectory = new File(args[++i]);
			else if (args[i].equals("--ignore"))
			{
				addRegex(ignoreRegexes, args[++i]);
			}
			else if (args[i].equals("--includeClasses"))
			{
				addRegex(includeClassesRegexes, args[++i]);
			}
			else if (args[i].equals("--excludeClasses"))
			{
				addRegex(excludeClassesRegexes, args[++i]);
			}
			else
			{
				FileInfo fileInfo = new FileInfo(baseDir, args[i]);
				filePaths.add(fileInfo);
			}
		}

		// Load coverage data
		if (dataFile.isFile())
			projectData = CoverageDataFileHandler.loadCoverageData(dataFile);
		if (projectData == null)
			projectData = new ProjectData();
		
		// Instrument classes
		System.out.println("Instrumenting "	+ filePaths.size() + " "
				+ (filePaths.size() == 1 ? "file" : "files")
				+ (destinationDirectory != null ? " to "
						+ destinationDirectory.getAbsoluteFile() : ""));

		Iterator iter = filePaths.iterator();
		while (iter.hasNext())
		{
			FileInfo fileInfo = (FileInfo)iter.next();
			if (fileInfo.isArchive()) {
				addInstrumentationToArchive(fileInfo);
			} else {
				addInstrumentation(fileInfo);
			}
		}

		// Save coverage data
		CoverageDataFileHandler.saveCoverageData(projectData, dataFile);
	}

	private static void addRegex(Collection list, String regex) {
		try
		{
			Perl5Compiler pc = new Perl5Compiler();
			Pattern pattern = pc.compile(regex);
			list.add(pattern);
		}
		catch (MalformedPatternException e)
		{
			logger.warn("The regular expression " + regex
					+ " is invalid: " + e.getLocalizedMessage());
		}
	}

	public static void main(String[] args)
	{
		Header.print(System.out);

		long startTime = System.currentTimeMillis();

		Main main = new Main();

		try {
			args = CommandLineBuilder.preprocessCommandLineArguments( args);
		} catch( Exception ex) {
			System.err.println( "Error: Cannot process arguments: " + ex.getMessage());
			System.exit(1);
		}
		main.parseArguments(args);

		long stopTime = System.currentTimeMillis();
		System.out.println("Instrument time: " + (stopTime - startTime) + "ms");
	}

}
