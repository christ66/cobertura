/*
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

package net.sourceforge.cobertura.reporting;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.cobertura.coverage.CoverageData;
import net.sourceforge.cobertura.util.ClassHelper;

public final class Clazz implements Comparable
{

	private Map lines;
	private String packageName;
	private String name;
	private CoverageData rawCoverageData;

	private Clazz(String longName)
	{
		if (longName == null)
		{
			throw new IllegalArgumentException("Class name can not be null.");
		}
		lines = new TreeMap();
		packageName = ClassHelper.getPackageName(longName);
		name = ClassHelper.getBaseName(longName);
	}
	
	public Clazz(String longName, CoverageData data) {
	    this(longName);

		setRawCoverageData(data);

		Iterator iter = data.getValidLineNumbers().iterator();
		while (iter.hasNext())
		{
			int lineNumber = ((Integer)iter.next()).intValue();
			long numberOfHits = data.getHitCount(lineNumber);
			addLine(lineNumber, numberOfHits);
		}
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
		if (getNumberOfBranches() == 0)
		{
			if (getNumberOfCoveredLines() == 0)
			{
				return 0;
			}
			return 1;
		}
		return (double)getNumberOfCoveredBranches() / (double)getNumberOfBranches();
	}

	public double getLineCoverageRate()
	{
		if (getNumberOfLines() == 0)
		{
			return 1;
		}
		return (double)getNumberOfCoveredLines() / (double)getNumberOfLines();
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
		return getRawCoverageData().getNumberOfValidBranches();
	}

	public int getNumberOfCoveredBranches()
	{
		return getRawCoverageData().getNumberOfCoveredBranches();
	}

	public int getNumberOfCoveredLines()
	{
		return getRawCoverageData().getNumberOfCoveredLines();
	}

	public long getNumberOfHits(int lineNumber)
	{
		Long numberOfHits = (Long)lines.get(new Integer(lineNumber));
		return numberOfHits.longValue();
	}

	public int getNumberOfLines()
	{
		return getRawCoverageData().getNumberOfValidLines();
	}

	public String getPackageName()
	{
		return packageName;
	}

	public final CoverageData getRawCoverageData()
	{
		return rawCoverageData;
	}

	public boolean isValidSourceLine(int lineNumber)
	{
		return lines.containsKey(new Integer(lineNumber));
	}

	private final void setRawCoverageData(CoverageData rawCoverageData)
	{
		this.rawCoverageData = rawCoverageData;
	}

}
