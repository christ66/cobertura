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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * CoverageData information is typically serialized to a file.
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

	private static final long serialVersionUID = 3;

	/**
	 * Each key is a line number in this class, stored as an Integer object.
	 * Each value is information about the class, stored as a ClassData object.
	 */
	private Map classes = new HashMap();

	public CoverageData()
	{
	}

	public void addClassData(ClassData classData)
	{
		if (classes.containsKey(classData.getSourceFileName()))
			throw new IllegalArgumentException(
					"Coverage data already contains a class with the source file name "
							+ classData.getSourceFileName());
		classes.put(classData.getSourceFileName(), classData);
	}

	/**
	 * Returns true if the given object is an instance of the
	 * CoverageData class, it contains the same number of classes
	 * as this instance, and the classes in the two instances
	 * are all equal.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj instanceof CoverageData))
			return false;

		CoverageData coverageData = (CoverageData)obj;
		return classes.equals(coverageData.classes);
	}

	public Collection getClasses()
	{
		return classes.values();
	}

	public int getNumberOfClasses()
	{
		return classes.size();
	}
}