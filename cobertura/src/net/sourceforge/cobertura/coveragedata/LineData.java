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

import java.io.Serializable;

/**
 * <p>
 * This class implements HasBeenInstrumented so that when cobertura
 * instruments itself, it will omit this class.  It does this to
 * avoid an infinite recursion problem because instrumented classes
 * make use of this class.
 * </p>
 */
public class LineData
		implements CoverageData, HasBeenInstrumented, Serializable
{
	private static final long serialVersionUID = 3;

	private long hits;
	private boolean isBranch;
	private final int lineNumber;
	private String methodDescriptor;
	private String methodName;

	LineData(int lineNumber)
	{
		this(lineNumber, null, null);
	}

	LineData(int lineNumber, String methodName, String methodDescriptor)
	{
		this.hits = 0;
		this.isBranch = false;
		this.lineNumber = lineNumber;
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		LineData lineData = (LineData)obj;
		return (this.hits == lineData.hits)
				&& (this.isBranch == lineData.isBranch)
				&& (this.lineNumber == lineData.lineNumber)
				&& (this.methodDescriptor.equals(lineData.methodDescriptor))
				&& (this.methodName.equals(lineData.methodName));
	}

	public double getBranchCoverageRate()
	{
		return (!isBranch || (getHits() > 0)) ? 1 : 0;
	}

	public long getHits()
	{
		return hits;
	}

	public double getLineCoverageRate()
	{
		return (getHits() > 0) ? 1 : 0;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

	public String getMethodDescriptor()
	{
		return methodDescriptor;
	}

	public String getMethodName()
	{
		return methodName;
	}

	public int getNumberOfCoveredBranches()
	{
		return (isBranch() && (getHits() > 0)) ? 1 : 0;
	}

	public int getNumberOfCoveredLines()
	{
		return (getHits() > 0) ? 1 : 0;
	}

	public int getNumberOfValidBranches()
	{
		return (isBranch()) ? 1 : 0;
	}

	public int getNumberOfValidLines()
	{
		return 1;
	}

	public int hashCode()
	{
		return this.lineNumber;
	}

	public boolean isBranch()
	{
		return isBranch;
	}

	void setBranch(boolean isBranch)
	{
		this.isBranch = isBranch;
	}

	void setMethodNameAndDescriptor(String name, String descriptor)
	{
		this.methodName = name;
		this.methodDescriptor = descriptor;
	}

	void touch()
	{
		this.hits++;
	}

}
