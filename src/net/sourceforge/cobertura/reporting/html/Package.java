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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Package implements Comparable
{

	private String name;
	private Map classes;

	public Package(String name)
	{
		if (name == null)
		{
			throw new IllegalArgumentException(
					"Package name can not be null.");
		}

		this.name = name;
		classes = new TreeMap();
	}

	public void addClass(Clazz clazz)
	{
		if (classes.containsKey(clazz.getName()))
		{
			throw new IllegalArgumentException(
					"This package already contains the class "
							+ clazz.getName());
		}
		classes.put(clazz.getName(), clazz);
	}

	/**
	 * Required when implementing Comparable.
	 */
	public int compareTo(Object o1)
	{
		String name1 = ((Package)o1).getName();
		return name.compareToIgnoreCase(name1);
	}

	public double getBranchCoverageRate()
	{
		long numberOfBranches = getNumberOfBranches();
		if (numberOfBranches == 0)
		{
			if (getNumberOfCoveredLines() == 0)
				return 0;
			return 1;
		}
		return (double)getNumberOfCoveredBranches()
				/ (double)numberOfBranches;
	}

	public Set getClasses()
	{
		return new TreeSet(classes.values());
	}

	public String getFileName()
	{
		return name.replace('.', '/');
	}

	public double getLineCoverageRate()
	{
		long numberOfLines = getNumberOfLines();
		if (numberOfLines == 0)
		{
			return 1;
		}
		return (double)getNumberOfCoveredLines() / (double)numberOfLines;
	}

	public String getName()
	{
		return name;
	}

	public long getNumberOfBranches()
	{
		long numberOfBranches = 0;

		Iterator iter = classes.values().iterator();
		while (iter.hasNext())
		{
			Clazz clazz = (Clazz)iter.next();
			numberOfBranches += clazz.getNumberOfBranches();
		}

		return numberOfBranches;
	}

	public long getNumberOfCoveredBranches()
	{
		long numberOfCoveredBranches = 0;

		Iterator iter = classes.values().iterator();
		while (iter.hasNext())
		{
			Clazz clazz = (Clazz)iter.next();
			numberOfCoveredBranches += clazz.getNumberOfCoveredBranches();
		}

		return numberOfCoveredBranches;
	}

	public long getNumberOfCoveredLines()
	{
		long numberOfCoveredLines = 0;

		Iterator iter = classes.values().iterator();
		while (iter.hasNext())
		{
			Clazz clazz = (Clazz)iter.next();
			numberOfCoveredLines += clazz.getNumberOfCoveredLines();
		}

		return numberOfCoveredLines;
	}

	public long getNumberOfLines()
	{
		long numberOfLines = 0;

		Iterator iter = classes.values().iterator();
		while (iter.hasNext())
		{
			Clazz clazz = (Clazz)iter.next();
			numberOfLines += clazz.getNumberOfLines();
		}

		return numberOfLines;
	}

	public void removeClass(Clazz clazz)
	{
		classes.remove(clazz.getName());
	}

}