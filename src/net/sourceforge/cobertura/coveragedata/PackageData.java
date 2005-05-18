/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
 * Copyright (C) 2005 Jeremy Thomerson <jthomerson@users.sourceforge.net>
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

import org.apache.log4j.Logger;

import net.sourceforge.cobertura.util.StringUtil;

/**
 * @author Mark Doliner
 * @author Jeremy Thomerson
 */
public class PackageData extends CoverageDataContainer implements Comparable, HasBeenInstrumented
{

	private static final long serialVersionUID = 3;
	private static final Logger LOGGER = Logger.getLogger(PackageData.class);

	private String name;

	public PackageData(String name)
	{
		if (name == null)
			throw new IllegalArgumentException(
					"Package name must be specified.");
		this.name = name;
	}

	/**
	 * @param fullName
	 * @return The full name of the class without $Foo on inner classes (i.e. Class$Foo)
	 */
	private String getClassNameIgnoreInner(String fullName) {
	    if (fullName.indexOf('$') != -1) {
            return StringUtil.replaceAll(fullName, fullName.substring(fullName.lastIndexOf('$')), "");
	    }
	    return fullName;
	}

	public ClassData getClassData(String fullClassName) {
	    String keyName = new ClassData(getClassNameIgnoreInner(fullClassName)).getBaseName();
	    return (ClassData) children.get(keyName);
	}

	public void addClassData(ClassData classData)
	{
	    // this method aggregates data if classData is an inner class
	    //  with the data from the top level class that it is contained in
	    LOGGER.debug("addClassData: " + classData.getName());
	    String parentClassName = getClassNameIgnoreInner(classData.getName());
	    LOGGER.debug("\tparentClassName = " + parentClassName);
	    String keyName = new ClassData(parentClassName).getBaseName();
	    LOGGER.debug("\tkeyName = " + keyName);
	    
	    ClassData parent = getClassData(classData.getName());
        if (parent == null) {
            LOGGER.debug("\tno parent");
            parent = new ClassData(parentClassName);
        }
        classData.merge(parent);

		// Each key is a class basename, stored as an String object.
		// Each value is information about the class, stored as a ClassData object.
        LOGGER.debug("putting " + keyName + " = " + classData.getName());
		children.put(keyName, classData);
	}

	/**
	 * This is required because we implement Comparable.
	 */
	public int compareTo(Object o)
	{
		if (!o.getClass().equals(PackageData.class))
			return Integer.MAX_VALUE;
		return this.name.compareTo(((PackageData)o).name);
	}

	public boolean contains(String name)
	{
		return this.children.containsKey(name);
	}

	/**
	 * Returns true if the given object is an instance of the
	 * PackageData class, and it contains the same data as this
	 * class.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		PackageData packageData = (PackageData)obj;
		return super.equals(obj) && this.name.equals(packageData.name);
	}

	public String getName()
	{
		return this.name;
	}

	public String getSourceFileName()
	{
		return this.name.replace('.', '/');
	}

	public int hashCode()
	{
		return this.name.hashCode();
	}

}
