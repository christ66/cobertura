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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.cobertura.util.ClassHelper;

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
	 * Each key is a package name, stored as an String object.
	 * Each value is information about the package, stored as a PackageData object.
	 */
	private Map packages = new HashMap();

	public CoverageData()
	{
	}

	public void addClassData(ClassData classData)
	{
		String packageName = ClassHelper.getPackageName(classData.getName());
		PackageData packageData = (PackageData)packages.get(packageName);
		if (packageData == null)
		{
			packageData = new PackageData(packageName);
			packages.put(packageName, packageData);
		}
		packageData.addClassData(classData);
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
		return this.packages.equals(coverageData.packages);
	}

	public ClassData getClassData(String name)
	{
		String packageName = ClassHelper.getPackageName(name);
		String baseName = ClassHelper.getBaseName(name);
		PackageData packageData = (PackageData)packages.get(packageName);
		if (packageData == null)
			return null;
		return packageData.getClassData(baseName);
	}

	public Collection getClasses()
	{
		HashSet classes = new HashSet();
		Iterator iter = packages.values().iterator();
		while (iter.hasNext())
		{
			PackageData packageData = (PackageData)iter.next();
			classes.addAll(packageData.getClasses());
		}
		return classes;
	}

	public int getNumberOfClasses()
	{
		int numberOfClasses = 0;
		Iterator iter = packages.values().iterator();
		while (iter.hasNext())
		{
			PackageData packageData = (PackageData)iter.next();
			numberOfClasses += packageData.getNumberOfClasses();
		}
		return numberOfClasses;
	}

	public int getNumberOfPackages()
	{
		return packages.size();
	}

	public Collection getPackages()
	{
		return packages.values();
	}

}
