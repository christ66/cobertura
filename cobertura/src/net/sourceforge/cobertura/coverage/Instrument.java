/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
 * Copyright (C) 2005 Joakim Erdfelt <joakim@erdfelt.net
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.cobertura.util.ClassHelper;
import net.sourceforge.cobertura.util.JavaClassHelper;

import org.apache.bcel.classfile.JavaClass;
import org.apache.log4j.Logger;

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
 * do the following
 * 
 * <ol>
 * <li>Get an instance of the CoverageData class.</li>
 * <li>Call a method in this CoverageData class that increments
 * a counter for this line of code.
 * </ol>
 *
 * After every line in a class has been "instrumented," Cobertura
 * edits the bytecode for the class one more time and adds an 
 * "implements net.sourceforge.cobertura.coverage.HasBeenInstrumented"  This 
 * is basically just a flag used internally by Cobertura to 
 * determine whether a class has been instrumented or not.
 * </p>
 */
public class Instrument
{

	private static final Logger logger = Logger.getLogger(Instrument.class);

	private File destinationDirectory = null;
	private String ignoreRegex = null;
	private File baseDir = null;

	/**
	 * @param javaClass A compiled Java class.
	 * @return True if the specified class implements the interface
	 * {@link HasBeenInstrumented}, otherwise false.
	 */
	private static boolean isAlreadyInstrumented(JavaClass javaClass)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("javaClass: " + javaClass.getClassName());
		}

		String[] interfaceNames = javaClass.getInterfaceNames();
		for (int i = 0; i < interfaceNames.length; i++)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug(javaClass.getClassName() + " implements "
						+ interfaceNames[i]);
			}

			if (interfaceNames[i].equals(HasBeenInstrumented.class.getName()))
			{
				if (logger.isInfoEnabled())
				{
					logger.info(javaClass.getClassName()
							+ " has already been instrumented");
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * @param file A file.
	 * @return True if the specified file has "class" as its extension,
	 * false otherwise.
	 */
	private static boolean isClass(File file)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("file: " + file.getName());
		}

		return file.getName().endsWith(".class");
	}

	/**
	 * @param javaClass A compiled Java class
	 * @return True if the given class is an interface.
	 */
	private static boolean isInterface(JavaClass javaClass)
	{
		return !javaClass.isClass();
	}

	/**
	 * Add coverage instrumentation to the specified Java class.
	 * @param clazz A Java .class file.
	 */
	private void instrument(File clazz)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("name: " + clazz.getName());
		}

		InputStream fileInputStream = null;

		try
		{
			fileInputStream = new FileInputStream(clazz);
			JavaClass javaClass = JavaClassHelper.newJavaClass(
					fileInputStream, clazz.getName());
			fileInputStream.close();
			fileInputStream = null;

			if (isInterface(javaClass) || isAlreadyInstrumented(javaClass))
			{
				if (destinationDirectory != null)
				{
					/**
					 * It is not normally necessary to do anything with an
					 * interface or class that has already been
					 * instrumented. However, if a destination directory has
					 * been specified we copy it to the destination directory,
					 * so that on subsequent invocations of "ant" the files will
					 * be seen as being upto date, and will not require
					 * instrumentation.
					 */
					File outputDirectory = new File(destinationDirectory,
							ClassHelper.getPackageName(
									javaClass.getClassName()).replace('.',
									'/'));
					outputDirectory.mkdirs();
					javaClass.dump(new File(outputDirectory, ClassHelper
							.getBaseName(javaClass.getClassName())
							+ ".class"));
				}
				return;
			}

			if (logger.isInfoEnabled())
			{
				logger.info("instrumenting " + javaClass.getClassName());
			}

			InstrumentClassGen instrument = new InstrumentClassGen(javaClass,
					ignoreRegex);
			instrument.addInstrumentation();

			if (logger.isDebugEnabled())
			{
				JavaClassHelper.dump(instrument.getJavaClass());
			}

			if (destinationDirectory == null)
			{
				instrument.getJavaClass().dump(clazz);
			}
			else
			{
				File outputDirectory = new File(destinationDirectory,
						ClassHelper.getPackageName(javaClass.getClassName())
								.replace('.', '/'));
				outputDirectory.mkdirs();
				instrument.getJavaClass().dump(
						new File(outputDirectory, ClassHelper
								.getBaseName(javaClass.getClassName())
								+ ".class"));
			}

			CoverageDataInternal i = CoverageDataInternalFactory
					.getInstance().newInstrumentation(
							javaClass.getClassName());

			if (instrument.getSourceLineNumbers().isEmpty())
			{
				/*
				 * TODO: A common cause for the following error is the
				 * attempted instrumentation of nested classes.  For
				 * example, HelloWorld$Helper.class.  Somehow someone
				 * should find a way to tell the nested/internal class
				 * to look at it's containing class for line number
				 * information.  Would that require a change to javac?
				 */
				logger.warn("No source line numbers found for: "
						+ javaClass.getClassName()
						+ ", compile with debug=\"yes\".");
			}

			i.setSourceFileName(javaClass.getSourceFileName());
			i.setSourceLineNumbers(instrument.getSourceLineNumbers());
			i.setSourceLineNumbersByMethod(instrument.getMethodLineNumbers());
			i.setConditionals(instrument.getSourceConditionals());
			i.setConditionalsByMethod(instrument.getMethodConditionals());
			i.setMethodNamesAndSignatures(instrument
					.getMethodNamesAndSignatures());
		}
		catch (IOException ex)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug(ex);
			}
			throw new CoverageRuntimeException(ex);
		}
		finally
		{
			if (fileInputStream != null)
			{
				try
				{
					fileInputStream.close();
				}
				catch (IOException whileClosing)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug(whileClosing);
					}
				}
			}
		}
	}

	private void addInstrumentation(File file)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("file: " + file.getAbsolutePath());
		}

		if (file.isDirectory())
		{
			File[] contents = file.listFiles();
			for (int i = 0; i < contents.length; i++)
			{
				addInstrumentation(contents[i]);
			}
		}
		else if (isClass(file))
		{
			instrument(file);
		}
	}

	private void addInstrumentation(String filename)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("filename: " + filename);
		}

		if (baseDir == null)
		{
			addInstrumentation(new File(filename));
		}
		else
		{
			addInstrumentation(new File(baseDir, filename));
		}
	}

	private void addInstrumentation(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-d"))
			{
				destinationDirectory = new File(args[++i]);
				continue;
			}
			else if (args[i].equals("-basedir"))
			{
				baseDir = new File(args[++i]);
				continue;
			}
			else if (args[i].equals("-ignore"))
			{
				ignoreRegex = args[++i];
				continue;
			}

			addInstrumentation(args[i]);
		}
	}

	public static void main(String[] args)
	{
		long startTime = System.currentTimeMillis();

		Instrument instrument = new Instrument();

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
			BufferedReader bufferedReader = null;

			try
			{
				bufferedReader = new BufferedReader(new FileReader(
						commandsFileName));
				String line;

				while ((line = bufferedReader.readLine()) != null)
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

		instrument.addInstrumentation(args);

		long stopTime = System.currentTimeMillis();
		System.out.println("Instrument time: " + (stopTime - startTime)
				+ "ms");
	}
}
