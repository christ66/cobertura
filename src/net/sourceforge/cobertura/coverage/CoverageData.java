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

package net.sourceforge.cobertura.coverage;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * CoverageData information is typically serialized to a file. An
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
public class CoverageData implements HasBeenInstrumented, Serializable
{

	private static final long serialVersionUID = 2;

	/**
	 * Each key is a line number in this class, stored as an Integer object.
	 * Each value is information about the line, stored as a LineInformation object.
	 */
	private Map conditionals = new HashMap();

	/**
	 * Each key is a line number in this class, stored as an Integer object.
	 * Each value is information about the line, stored as a LineInformation object.
	 */
	private Map lines = new HashMap();

	private Set methodNamesAndDescriptors = new HashSet();

	private String sourceFileName = null;

	public void addLine(int lineNumber, String methodName,
			String methodDescriptor)
	{
		LineInformation lineInformation = getLineInformation(lineNumber);
		if (lineInformation == null)
		{
			lineInformation = new LineInformation(lineNumber);
			lines.put(new Integer(lineNumber), lineInformation);
		}
		lineInformation.setMethodNameAndDescriptor(methodName,
				methodDescriptor);
		methodNamesAndDescriptors.add(methodName + methodDescriptor);
	}

	/**
	 * @return The branch coverage rate for the class.
	 */
	public double getBranchCoverageRate()
	{
		if (conditionals.size() == 0)
		{
			// no conditional branches, therefore 100% branch coverage.
			return 1d;
		}
		return (double)getNumberOfCoveredBranches() / conditionals.size();
	}

	/**
	 * @return The branch coverage rate for a particular method.
	 */
	public double getBranchCoverageRate(String methodNameAndDescriptor)
	{
		int total = 0;
		int hits = 0;

		Iterator iter = conditionals.values().iterator();
		while (iter.hasNext())
		{
			LineInformation next = (LineInformation)iter.next();
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
		return (double)hits / total;
	}

	public Set getConditionals()
	{
		return Collections.unmodifiableSet(conditionals.keySet());
	}

	/**
	 * @param lineNumber The source code line number.
	 * @return The number of hits a particular line of code has.
	 */
	public long getHitCount(int lineNumber)
	{
		Integer lineNum = new Integer(lineNumber);
		if (!lines.containsKey(lineNum))
		{
			return 0;
		}

		return ((LineInformation)lines.get(lineNum)).getHits();
	}

	/**
	 * @return The line coverage rate for the class
	 */
	public double getLineCoverageRate()
	{
		if (lines.size() == 0)
		{
			return 1d;
		}
		return (double)getNumberOfCoveredLines() / lines.size();
	}

	/**
	 * @return The line coverage rate for particular method
	 */
	public double getLineCoverageRate(String methodNameAndDescriptor)
	{
		int total = 0;
		int hits = 0;

		Iterator iter = lines.values().iterator();
		while (iter.hasNext())
		{
			LineInformation next = (LineInformation)iter.next();
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
		return (double)hits / total;
	}

	private LineInformation getLineInformation(int lineNumber)
	{
		return (LineInformation)lines.get(new Integer(lineNumber));
	}

	/**
	 * @return The method name and descriptor of each method found in the
	 *         class represented by this instrumentation.
	 */
	public Set getMethodNamesAndDescriptors()
	{
		return methodNamesAndDescriptors;
	}

	/**
	 * @return The number of branches in this class covered by testing.
	 */
	public int getNumberOfCoveredBranches()
	{
		int num = 0;

		Iterator iter = conditionals.values().iterator();
		while (iter.hasNext())
		{
			if (((LineInformation)iter.next()).getHits() > 0)
				num++;
		}

		return num;
	}

	/**
	 * @return The number of lines in this class covered by testing.
	 */
	public int getNumberOfCoveredLines()
	{
		int num = 0;

		Iterator iter = lines.values().iterator();
		while (iter.hasNext())
		{
			if (((LineInformation)iter.next()).getHits() > 0)
				num++;
		}

		return num;
	}

	/**
	 * @return The number of branches in this class.
	 */
	public int getNumberOfValidBranches()
	{
		return conditionals.size();
	}

	/**
	 * @return The number of lines in this class.
	 */
	public int getNumberOfValidLines()
	{
		return lines.size();
	}

	public String getSourceFileName()
	{
		return sourceFileName;
	}

	/**
	 * @return The set of valid source line numbers
	 */
	public Set getValidLineNumbers()
	{
		return Collections.unmodifiableSet(lines.keySet());
	}

	/**
	 * @return True if the line contains a branch statement.
	 */
	public boolean isBranch(int lineNumber)
	{
		return conditionals.containsKey(new Integer(lineNumber));
	}

	/**
	 * Determine if a given line number is a valid line of code.
	 *
	 * @return True if the line contains executable code.  False
	 *         if the line is empty, or a comment, etc.
	 */
	public boolean isValidSourceLineNumber(int lineNumber)
	{
		return lines.containsKey(new Integer(lineNumber));
	}

	public void markLineAsConditional(int lineNumber)
	{
		LineInformation lineInformation = getLineInformation(lineNumber);
		if (lineInformation != null)
		{
			lineInformation.setConditional(true);
			this.conditionals.put(new Integer(lineNumber), lineInformation);
		}
	}

	/**
	 * Merge some existing instrumentation with this instrumentation.
	 *
	 * @param coverageData Some existing coverage data.
	 */
	public void merge(CoverageData coverageData)
	{
		lines.putAll(coverageData.lines);
		conditionals.putAll(coverageData.conditionals);
		methodNamesAndDescriptors.addAll(coverageData
				.getMethodNamesAndDescriptors());
	}

	public void removeLine(int lineNumber)
	{
		lines.remove(new Integer(lineNumber));
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
		LineInformation lineInformation = getLineInformation(lineNumber);
		if (lineInformation != null)
			lineInformation.touch();
	}

}