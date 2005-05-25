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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.cobertura.util.StringUtil;

public class SourceFileData extends CoverageDataContainer
		implements Comparable, HasBeenInstrumented
{

	private static final long serialVersionUID = 1;

	private String name;

	// TODO: If the source file name must end in .java, then check for that.
	public SourceFileData(String name)
	{
		if (name == null)
			throw new IllegalArgumentException(
					"Source file name must be specified.");
		this.name = name;
	}

	public void addClassData(ClassData classData)
	{
		if (children.containsKey(classData.getBaseName()))
			throw new IllegalArgumentException("Source file " + this.name
					+ " already contains a class with the name "
					+ classData.getBaseName());

		// Each key is a class basename, stored as an String object.
		// Each value is information about the class, stored as a ClassData object.
		children.put(classData.getBaseName(), classData);
	}

	/**
	 * This is required because we implement Comparable.
	 */
	public int compareTo(Object o)
	{
		if (!o.getClass().equals(SourceFileData.class))
			return Integer.MAX_VALUE;
		return this.name.compareTo(((SourceFileData)o).name);
	}

	public boolean contains(String name)
	{
		return this.children.containsKey(name);
	}

	/**
	 * Returns true if the given object is an instance of the
	 * SourceFileData class, and it contains the same data as this
	 * class.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		SourceFileData sourceFileData = (SourceFileData)obj;
		return super.equals(obj) && this.name.equals(sourceFileData.name);
	}

	public String getBaseName()
	{
		String fullNameWithoutExtension;
		int lastDot = this.name.lastIndexOf('.');
		if (lastDot == -1)
		{
			fullNameWithoutExtension = this.name;
		}
		else
		{
			fullNameWithoutExtension = this.name.substring(0, lastDot);
		}

		int lastSlash = fullNameWithoutExtension.lastIndexOf('/');
		if (lastSlash == -1)
		{
			return fullNameWithoutExtension;
		}
		return fullNameWithoutExtension.substring(lastSlash + 1);
	}

	public SortedSet getClasses()
	{
		return new TreeSet(this.children.values());
	}

	public long getHitCount(int lineNumber)
	{
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			ClassData classData = (ClassData)iter.next();
			if (classData.isValidSourceLineNumber(lineNumber))
				return classData.getHitCount(lineNumber);
		}
		return 0;
	}

	public String getName()
	{
		return this.name;
	}

	public String getNormalizedName()
	{
		String fullNameWithoutExtension;
		int lastDot = this.name.lastIndexOf('.');
		if (lastDot == -1)
		{
			fullNameWithoutExtension = this.name;
		}
		else
		{
			fullNameWithoutExtension = this.name.substring(0, lastDot);
		}

		return StringUtil.replaceAll(fullNameWithoutExtension, "/", ".");
	}

	public String getPackageName()
	{
		int lastSlash = this.name.lastIndexOf('/');
		if (lastSlash == -1)
		{
			return this.name;
		}
		return StringUtil.replaceAll(this.name.substring(0, lastSlash), "/",
				".");
	}

	public int hashCode()
	{
		return this.name.hashCode();
	}

	public boolean isValidSourceLineNumber(int lineNumber)
	{
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			ClassData classData = (ClassData)iter.next();
			if (classData.isValidSourceLineNumber(lineNumber))
				return true;
		}
		return false;
	}

}
