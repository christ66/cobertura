/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
 * Copyright (C) 2005 Joakim Erdfelt <joakim@erdfelt.net>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package net.sourceforge.cobertura.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.SourceFileScanner;

public abstract class CommonMatchingTask extends MatchingTask
{

	private static final String LINESEP = System
			.getProperty("line.separator");

	final String className;
	final List fileSets = new LinkedList();

	private Java java = null;
	private File commandLineFile = null;
	private FileWriter commandLineWriter = null;

	File toDir = null;

	public CommonMatchingTask(String className)
	{
		this.className = className;
	}

	private String getClassName()
	{
		return className;
	}

	protected void initArgs()
	{
		try
		{
			commandLineFile = File.createTempFile("cobertura.", ".cmdline");
			commandLineWriter = new FileWriter(commandLineFile);
		}
		catch (IOException ioe)
		{
			getProject().log(
					"Error initializing commands file "
							+ commandLineFile.getAbsolutePath(),
					Project.MSG_ERR);
			throw new BuildException("Unable to initialize commands file.");
		}
	}

	protected void addArg(String arg)
	{
		try
		{
			commandLineWriter.write(arg + LINESEP);
		}
		catch (IOException ioe)
		{
			getProject().log(
					"Error writing commands file "
							+ commandLineFile.getAbsolutePath(),
					Project.MSG_ERR);
			throw new BuildException("Unable to write to commands file.");
		}
	}

	protected void saveArgs()
	{
		try
		{
			commandLineWriter.flush();
			commandLineWriter.close();
		}
		catch (IOException ioe)
		{
			getProject().log(
					"Error saving commands file "
							+ commandLineFile.getAbsolutePath(),
					Project.MSG_ERR);
			throw new BuildException("Unable to save the commands file.");
		}
		/* point to commands file */
		getJava().createArg().setValue("-commandsfile");
		getJava().createArg().setValue(commandLineFile.getAbsolutePath());
	}

	protected Java getJava()
	{
		if (java == null)
		{
			java = (Java)getProject().createTask("java");
			java.setTaskName(getTaskName());
			java.setClassname(getClassName());
			java.setFork(true);
			java.setDir(getProject().getBaseDir());

			if (getClass().getClassLoader() instanceof AntClassLoader)
			{
				String classpath = ((AntClassLoader)getClass()
						.getClassLoader()).getClasspath();
				createClasspath().setPath(classpath.replaceAll("%20", " "));
			}
			else if (getClass().getClassLoader() instanceof URLClassLoader)
			{
				URL[] earls = ((URLClassLoader)getClass().getClassLoader())
						.getURLs();
				for (int i = 0; i < earls.length; i++)
				{
					String classpath = earls[i].getFile();
					createClasspath().setPath(
							classpath.replaceAll("%20", " "));
				}
			}
		}

		return java;
	}

	public void setTodir(File toDir)
	{
		this.toDir = toDir;
	}

	public Path createClasspath()
	{
		return getJava().createClasspath().createPath();
	}

	public void setClasspath(Path classpath)
	{
		createClasspath().append(classpath);
	}

	public void setClasspathRef(Reference r)
	{
		createClasspath().setRefid(r);
	}

	DirectoryScanner getDirectoryScanner(FileSet fileSet)
	{
		return fileSet.getDirectoryScanner(getProject());
	}

	String[] getIncludedFiles(FileSet fileSet)
	{
		return getDirectoryScanner(fileSet).getIncludedFiles();
	}

	String[] getExcludedFiles(FileSet fileSet)
	{
		return getDirectoryScanner(fileSet).getExcludedFiles();
	}

	String[] getFilenames(FileSet fileSet)
	{
		String[] filesToReturn = getIncludedFiles(fileSet);

		if (toDir != null)
		{
			IdentityMapper m = new IdentityMapper();
			SourceFileScanner sfs = new SourceFileScanner(this);
			filesToReturn = sfs.restrict(getIncludedFiles(fileSet), fileSet
					.getDir(getProject()), toDir, m);
		}

		return filesToReturn;
	}

	String baseDir(FileSet fileSet)
	{
		return fileSet.getDirectoryScanner(getProject()).getBasedir()
				.toString();
	}

	public void addFileset(FileSet fileSet)
	{
		fileSets.add(fileSet);
	}
}
