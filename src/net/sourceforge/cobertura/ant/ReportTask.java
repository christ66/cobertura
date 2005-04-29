/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
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
import java.net.URL;
import java.net.URLClassLoader;

import net.sourceforge.cobertura.util.Header;
import net.sourceforge.cobertura.util.StringUtil;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Generate a coverage report based on coverage data generated 
 * by instrumented classes.
 */
public class ReportTask extends MatchingTask
{

	private String dataFile = null;
	private String format = "html";
	private Path src;
	private File destDir;

	private Java java = null;

	public void setDataFile(String dataFile)
	{
		this.dataFile = dataFile;
	}

	public void setDestDir(File destDir)
	{
		this.destDir = destDir;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}

	public void setSrcDir(Path srcDir)
	{
		if (src == null)
		{
			src = srcDir;
		}
		else
		{
			src.append(srcDir);
		}
	}

	public void execute() throws BuildException
	{
		Header.print(System.out);

		getJava().createArg().setValue("--format");
		getJava().createArg().setValue(format);

		if (dataFile != null)
		{
			getJava().createArg().setValue("--datafile");
			getJava().createArg().setValue(dataFile);
		}

		getJava().createArg().setValue("--destination");
		getJava().createArg().setValue(destDir.toString());

		getJava().createArg().setValue("--source");
		getJava().createArg().setValue(src.toString());

		if (getJava().executeJava() != 0)
		{
			throw new BuildException("Error generating report. See messages above.");
		}
	}

	protected Java getJava()
	{
		if (java == null)
		{
			java = (Java)getProject().createTask("java");
			java.setTaskName(getTaskName());
			java.setClassname("net.sourceforge.cobertura.reporting.Main");
			java.setFork(true);
			java.setDir(getProject().getBaseDir());

			if (getClass().getClassLoader() instanceof AntClassLoader)
			{
				String classpath = ((AntClassLoader)getClass()
						.getClassLoader()).getClasspath();
				createClasspath().setPath(
						StringUtil.replaceAll(classpath, "%20", " "));
			}
			else if (getClass().getClassLoader() instanceof URLClassLoader)
			{
				URL[] earls = ((URLClassLoader)getClass().getClassLoader())
						.getURLs();
				for (int i = 0; i < earls.length; i++)
				{
					String classpath = earls[i].getFile();
					createClasspath().setPath(
							StringUtil.replaceAll(classpath, "%20", " "));
				}
			}
		}
		return java;
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

}
