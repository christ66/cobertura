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
import java.util.HashSet;
import java.util.Iterator;

import net.sourceforge.cobertura.util.ClassHelper;

public class ProjectData extends CoverageDataContainer
{

	private static final long serialVersionUID = 3;

	public ProjectData()
	{
	}

	public void addClassData(ClassData classData)
	{
		String packageName = ClassHelper.getPackageName(classData.getName());
		PackageData packageData = (PackageData)children.get(packageName);
		if (packageData == null)
		{
			packageData = new PackageData(packageName);
			// Each key is a package name, stored as an String object.
			// Each value is information about the package, stored as a PackageData object.
			children.put(packageName, packageData);
		}
		packageData.addClassData(classData);
	}

	/**
	 * Returns true if the given object is an instance of the
	 * ProjectData class, it contains the same number of classes
	 * as this instance, and the classes in the two instances
	 * are all equal.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		//ProjectData coverageData = (ProjectData)obj;
		return super.equals(obj);
	}

	public ClassData getClassData(String name)
	{
		String packageName = ClassHelper.getPackageName(name);
		String baseName = ClassHelper.getBaseName(name);
		PackageData packageData = (PackageData)children.get(packageName);
		if (packageData == null)
			return null;
		return (ClassData)packageData.getChild(baseName);
	}

	public Collection getClasses()
	{
		HashSet classes = new HashSet();
		Iterator iter = children.values().iterator();
		while (iter.hasNext())
		{
			PackageData packageData = (PackageData)iter.next();
			classes.addAll(packageData.getChildren());
		}
		return classes;
	}

	public int getNumberOfClasses()
	{
		int numberOfClasses = 0;
		Iterator iter = children.values().iterator();
		while (iter.hasNext())
		{
			PackageData packageData = (PackageData)iter.next();
			numberOfClasses += packageData.getNumberOfChildren();
		}
		return numberOfClasses;
	}

}
