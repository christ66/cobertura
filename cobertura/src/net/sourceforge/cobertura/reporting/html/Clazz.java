/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

package net.sourceforge.cobertura.reporting.html;

import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.cobertura.util.ClassHelper;

public class Clazz implements Comparable
{

	private Map lines;
	private String packageName;
	private String name;
	private int numberOfBranches;
	private int numberOfCoveredBranches;
	private int numberOfCoveredLines;
	private int numberOfLines;

	public Clazz(String longName)
	{
		if (longName == null)
		{
			throw new IllegalArgumentException("Class name can not be null.");
		}
		lines = new TreeMap();
		packageName = ClassHelper.getPackageName(longName);
		name = ClassHelper.getBaseName(longName);
		numberOfBranches = 0;
		numberOfCoveredBranches = 0;
		numberOfCoveredLines = 0;
		numberOfLines = 0;
	}

	public void addLine(int lineNumber, long numberOfHits)
	{
		lines.put(new Integer(lineNumber), new Long(numberOfHits));
	}

	/**
	 * Required when implementing Comparable.
	 */
	public int compareTo(Object o1)
	{
		String longName = getLongName();
		String longName2 = ((Clazz)o1).getLongName();
		return longName.compareToIgnoreCase(longName2);
	}

	public double getBranchCoverageRate()
	{
		if (numberOfBranches == 0)
		{
			if (numberOfCoveredLines == 0)
			{
				return 0;
			}
			return 1;
		}
		return (double)numberOfCoveredBranches / (double)numberOfBranches;
	}

	public double getLineCoverageRate()
	{
		if (numberOfLines == 0)
		{
			return 1;
		}
		return (double)numberOfCoveredLines / (double)numberOfLines;
	}

	public String getLongFileName()
	{
		if (packageName.length() > 0)
		{
			return (packageName + "." + name).replace('.', '/') + ".java";
		}
		return name + ".java";
	}

	public String getLongName()
	{
		if (packageName.length() > 0)
		{
			return packageName + "." + name;
		}
		return name;
	}

	public String getName()
	{
		return name;
	}

	public int getNumberOfBranches()
	{
		return numberOfBranches;
	}

	public int getNumberOfCoveredBranches()
	{
		return numberOfCoveredBranches;
	}

	public int getNumberOfCoveredLines()
	{
		return numberOfCoveredLines;
	}

	public long getNumberOfHits(int lineNumber)
	{
		Long numberOfHits = (Long)lines.get(new Integer(lineNumber));
		return numberOfHits.longValue();
	}

	public int getNumberOfLines()
	{
		return numberOfLines;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public boolean isValidSourceLine(int lineNumber)
	{
		return lines.containsKey(new Integer(lineNumber));
	}

	public void setNumberOfBranches(int numberOfBranches)
	{
		this.numberOfBranches = numberOfBranches;
	}

	public void setNumberOfCoveredBranches(int numberOfCoveredBranches)
	{
		this.numberOfCoveredBranches = numberOfCoveredBranches;
	}

	public void setNumberOfCoveredLines(int numberOfCoveredLines)
	{
		this.numberOfCoveredLines = numberOfCoveredLines;
	}

	public void setNumberOfLines(int numberOfLines)
	{
		this.numberOfLines = numberOfLines;
	}

}
