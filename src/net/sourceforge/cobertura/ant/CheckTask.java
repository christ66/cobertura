/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * An ant task that can be used to optionally fail an ant build if
 * the coverage percentage for lines or branches is below a certain,
 * user specifiable threshold.
 */
public class CheckTask extends MatchingTask
{

	final Set regexes = new HashSet();

	protected String branchCoverageRate = null;
	protected String lineCoverageRate = null;

	private Java java = null;

	public void setBranch(String branchCoverageRate)
	{
		this.branchCoverageRate = branchCoverageRate;
	}

	public void setLine(String lineCoverageRate)
	{
		this.lineCoverageRate = lineCoverageRate;
	}

	public void execute() throws BuildException
	{
		if (lineCoverageRate != null)
		{
			getJava().createArg().setValue("-l");
			getJava().createArg().setValue(lineCoverageRate);
		}

		if (branchCoverageRate != null)
		{
			getJava().createArg().setValue("-b");
			getJava().createArg().setValue(branchCoverageRate);
		}

		Iterator i = regexes.iterator();
		while (i.hasNext())
		{
			getJava().createArg().setValue("-r");
			getJava().createArg().setValue(i.next().toString());
		}

		if (getJava().executeJava() != 0)
		{
			throw new BuildException();
		}
	}

	public Regex createRegex()
	{
		Regex regex = new Regex();
		regexes.add(regex);
		return regex;
	}

	protected Java getJava()
	{
		if (java == null)
		{
			java = (Java)getProject().createTask("java");
			java.setTaskName(getTaskName());
			java.setClassname("net.sourceforge.cobertura.check.Main");
			java.setFork(true);
			java.setDir(getProject().getBaseDir());

			if (getClass().getClassLoader() instanceof AntClassLoader)
			{
				createClasspath().setPath(
						((AntClassLoader)getClass().getClassLoader())
								.getClasspath());
			}
			else if (getClass().getClassLoader() instanceof URLClassLoader)
			{
				URL[] earls = ((URLClassLoader)getClass().getClassLoader())
						.getURLs();
				for (int i = 0; i < earls.length; i++)
				{
					createClasspath().setPath(earls[i].getFile());
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
