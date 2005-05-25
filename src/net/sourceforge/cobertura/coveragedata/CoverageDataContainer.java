/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Jeremy Thomerson
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * Coverage data information is typically serialized to a file.
 * </p>
 *
 * <p>
 * This class implements HasBeenInstrumented so that when cobertura
 * instruments itself, it will omit this class.  It does this to
 * avoid an infinite recursion problem because instrumented classes
 * make use of this class.
 * </p>
 */
public abstract class CoverageDataContainer
		implements CoverageData, HasBeenInstrumented, Serializable
{

	/**
	 * Each key is the name of a child, usually stored as a String or
	 * an Integer object.  Each value is information about the child,
	 * stored as an object that implements the CoverageData interface.
	 */
	Map children = new HashMap();

	/**
	 * Determine if this CoverageDataContainer is equal to
	 * another one.  Subclasses should override this and
	 * make sure they implement the hashCode method.
	 *
	 * @param obj An object to test for equality.
	 * @return True if the objects are equal.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		CoverageDataContainer coverageDataContainer = (CoverageDataContainer)obj;
		return this.children.equals(coverageDataContainer.children);
	}

	/**
	 * @return The average branch coverage rate for all children
	 *         in this container.
	 */
	public double getBranchCoverageRate()
	{
		int number = 0;
		int numberCovered = 0;
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			CoverageData coverageContainer = (CoverageData)iter.next();
			number += coverageContainer.getNumberOfValidBranches();
			numberCovered += coverageContainer.getNumberOfCoveredBranches();
		}
		if (number == 0)
		{
			// no branches, therefore 100% branch coverage.
			return 1d;
		}
		return (double)numberCovered / number;
	}

	/**
	 * Get a child from this container with the specified
	 * key.
	 * @param name The key used to lookup the child in the
	 *        map.
	 * @return The child object, if found, or null if not found.
	 */
	public CoverageData getChild(String name)
	{
		return (CoverageData)this.children.get(name);
	}

	/**
	 * Get all children of this container.
	 *
	 * @return A collection of CoverageData objects.
	 */
	/*
	 public Collection getChildren()
	 {
	 return this.children.values();
	 }
	 */
	/**
	 * @return The average line coverage rate for all children
	 *         in this container.  This number will be a decimal
	 *         between 0 and 1, inclusive.
	 */
	public double getLineCoverageRate()
	{
		int number = 0;
		int numberCovered = 0;
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			CoverageData coverageContainer = (CoverageData)iter.next();
			number += coverageContainer.getNumberOfValidLines();
			numberCovered += coverageContainer.getNumberOfCoveredLines();
		}
		if (number == 0)
		{
			// no lines, therefore 100% line coverage.
			return 1d;
		}
		return (double)numberCovered / number;
	}

	/**
	 * @return The number of children in this container.
	 */
	public int getNumberOfChildren()
	{
		return this.children.size();
	}

	public int getNumberOfCoveredBranches()
	{
		int number = 0;
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			CoverageData coverageContainer = (CoverageData)iter.next();
			number += coverageContainer.getNumberOfCoveredBranches();
		}
		return number;
	}

	public int getNumberOfCoveredLines()
	{
		int number = 0;
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			CoverageData coverageContainer = (CoverageData)iter.next();
			number += coverageContainer.getNumberOfCoveredLines();
		}
		return number;
	}

	public int getNumberOfValidBranches()
	{
		int number = 0;
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			CoverageData coverageContainer = (CoverageData)iter.next();
			number += coverageContainer.getNumberOfValidBranches();
		}
		return number;
	}

	public int getNumberOfValidLines()
	{
		int number = 0;
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			CoverageData coverageContainer = (CoverageData)iter.next();
			number += coverageContainer.getNumberOfValidLines();
		}
		return number;
	}

	/**
	 * It is highly recommended that classes extending this
	 * class override this hashCode method and generate a more
	 * effective hash code.
	 */
	public int hashCode()
	{
		return this.children.size();
	}

}
