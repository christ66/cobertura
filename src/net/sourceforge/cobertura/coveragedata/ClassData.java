/*
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

package net.sourceforge.cobertura.coveragedata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * ProjectData information is typically serialized to a file. An
 * instance of this class records coverage information for a single
 * class that has been instrumented.
 * </p>
 *
 * <p>
 * This class implements HasBeenInstrumented so that when cobertura
 * instruments itself, it will omit this class.  It does this to
 * avoid an infinite recursion problem because instrumented classes
 * make use of this class.
 * </p>
 */
public class ClassData extends CoverageDataContainer
		implements HasBeenInstrumented
{

	private static final long serialVersionUID = 3;

	/**
	 * Each key is a line number in this class, stored as an Integer object.
	 * Each value is information about the line, stored as a LineData object.
	 */
	private Map branches = new HashMap();

	private Set methodNamesAndDescriptors = new HashSet();

	private String name = null;
	private String sourceFileName = null;

	public ClassData(String name)
	{
		if (name == null)
			throw new IllegalArgumentException(
					"Class name must be specified.");
		this.name = name;
	}

	public void addLine(int lineNumber, String methodName,
			String methodDescriptor)
	{
		LineData lineData = getLineData(lineNumber);
		if (lineData == null)
		{
			lineData = new LineData(lineNumber);
			// Each key is a line number in this class, stored as an Integer object.
			// Each value is information about the line, stored as a LineData object.
			children.put(new Integer(lineNumber), lineData);
		}
		lineData.setMethodNameAndDescriptor(methodName, methodDescriptor);
		methodNamesAndDescriptors.add(methodName + methodDescriptor);
	}

	/**
	 * Returns true if the given object is an instance of the
	 * ClassData class, and it contains the same data as this
	 * class.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		ClassData classData = (ClassData)obj;
		return super.equals(obj)
				&& this.branches.equals(classData.branches)
				&& this.methodNamesAndDescriptors
						.equals(classData.methodNamesAndDescriptors)
				&& this.name.equals(classData.name)
				&& this.sourceFileName.equals(classData.sourceFileName);
	}

	public String getBaseName()
	{
		int lastDot = this.name.lastIndexOf('.');
		if (lastDot == -1)
		{
			return this.name;
		}
		return this.name.substring(lastDot + 1);
	}

	/**
	 * @return The branch coverage rate for a particular method.
	 */
	public double getBranchCoverageRate(String methodNameAndDescriptor)
	{
		int total = 0;
		int hits = 0;

		Iterator iter = branches.values().iterator();
		while (iter.hasNext())
		{
			LineData next = (LineData)iter.next();
			if (methodNameAndDescriptor.equals(next.getMethodName()
					+ next.getMethodDescriptor()))
			{
				total++;
				if (next.getHits() > 0)
				{
					hits++;
				}
			}
		}
		if (total == 0)
			return 1d;
		return (double)hits / total;
	}

	public Collection getBranches()
	{
		return Collections.unmodifiableCollection(branches.keySet());
	}

	/**
	 * @param lineNumber The source code line number.
	 * @return The number of hits a particular line of code has.
	 */
	public long getHitCount(int lineNumber)
	{
		Integer lineObject = new Integer(lineNumber);
		if (!children.containsKey(lineObject))
		{
			return 0;
		}

		return ((LineData)children.get(lineObject)).getHits();
	}

	/**
	 * @return The line coverage rate for particular method
	 */
	public double getLineCoverageRate(String methodNameAndDescriptor)
	{
		int total = 0;
		int hits = 0;

		Iterator iter = children.values().iterator();
		while (iter.hasNext())
		{
			LineData next = (LineData)iter.next();
			if (methodNameAndDescriptor.equals(next.getMethodName()
					+ next.getMethodDescriptor()))
			{
				total++;
				if (next.getHits() > 0)
				{
					hits++;
				}
			}
		}
		if (total == 0)
			return 1d;
		return (double)hits / total;
	}

	private LineData getLineData(int lineNumber)
	{
		return (LineData)children.get(new Integer(lineNumber));
	}

	/**
	 * @return The method name and descriptor of each method found in the
	 *         class represented by this instrumentation.
	 */
	public Set getMethodNamesAndDescriptors()
	{
		return methodNamesAndDescriptors;
	}

	public String getName()
	{
		return name;
	}

	/**
	 * @return The number of branches in this class.
	 */
	public int getNumberOfValidBranches()
	{
		return branches.size();
	}

	public String getPackageName()
	{
		int lastDot = this.name.lastIndexOf('.');
		if (lastDot == -1)
		{
			return "";
		}
		return this.name.substring(0, lastDot);
	}

	public String getSourceFileName()
	{
		if (sourceFileName == null)
			return null;
		String packageName = getPackageName();
		if (packageName == null)
			return sourceFileName;
		return getPackageName().replace('.', '/') + "/" + sourceFileName;
	}

	public int hashCode()
	{
		return this.name.hashCode();
	}

	/**
	 * @return True if the line contains a branch statement.
	 */
	public boolean isBranch(int lineNumber)
	{
		return branches.containsKey(new Integer(lineNumber));
	}

	/**
	 * Determine if a given line number is a valid line of code.
	 *
	 * @return True if the line contains executable code.  False
	 *         if the line is empty, or a comment, etc.
	 */
	public boolean isValidSourceLineNumber(int lineNumber)
	{
		return children.containsKey(new Integer(lineNumber));
	}

	public void markLineAsBranch(int lineNumber)
	{
		LineData lineData = getLineData(lineNumber);
		if (lineData != null)
		{
			lineData.setBranch(true);
			this.branches.put(new Integer(lineNumber), lineData);
		}
	}

	/**
	 * Merge some existing instrumentation with this instrumentation.
	 *
	 * @param coverageData Some existing coverage data.
	 */
	public void merge(ClassData coverageData)
	{
		children.putAll(coverageData.children);
		branches.putAll(coverageData.branches);
		methodNamesAndDescriptors.addAll(coverageData
				.getMethodNamesAndDescriptors());
	}

	public void removeLine(int lineNumber)
	{
		Integer lineObject = new Integer(lineNumber);
		children.remove(lineObject);
		branches.remove(lineObject);
	}

	public void setSourceFileName(String sourceFileName)
	{
		this.sourceFileName = sourceFileName;
	}

	/**
	 * Increment the number of hits for a particular line of code.
	 *
	 * @param lineNumber the line of code to increment the number of hits.
	 */
	public void touch(int lineNumber)
	{
		LineData lineData = getLineData(lineNumber);
		if (lineData != null)
			lineData.touch();
	}

}
