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

import java.io.File;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import net.sourceforge.cobertura.reporting.Util;
import net.sourceforge.cobertura.util.FileFinder;

public class PackageData extends CoverageDataContainer
		implements Comparable, HasBeenInstrumented
{

	private static final long serialVersionUID = 5;
    private static final Logger LOGGER = Logger.getLogger(PackageData.class);

	private String name;

	public PackageData(String name)
	{
		if (name == null)
			throw new IllegalArgumentException(
					"Package name must be specified.");
		this.name = name;
	}
    
    public double getCCN(FileFinder finder) {
        File[] files = finder.findDirectory(getSourceFileName());
        if (files.length == 0) {
            LOGGER.warn("No directories found for package: " + getSourceFileName());
        }

        double ccnSum = 0; 
        for (int i = 0; i < files.length; i++) {
            ccnSum += Util.getCCN(files[i], false);
        }
        
        return ccnSum / (double) files.length;
    }

	public void addClassData(ClassData classData)
	{
		String sourceFileName = classData.getSourceFileName();
		SourceFileData sourceFileData = (SourceFileData)children.get(sourceFileName);
		if (sourceFileData == null)
		{
			sourceFileData = new SourceFileData(sourceFileName);
			// Each key is a source file name, stored as an String object.
			// Each value is information about the source file, stored as
			// a SourceFileData object.
			this.children.put(sourceFileName, sourceFileData);
		}
		sourceFileData.addClassData(classData);
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

	public SortedSet getClasses()
	{
		SortedSet classes = new TreeSet();
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext()) {
			SourceFileData sourceFileData = (SourceFileData)iter.next();
			classes.addAll(sourceFileData.getClasses());
		}
		return classes;
	}

	public String getName()
	{
		return this.name;
	}

	public String getSourceFileName()
	{
		return this.name.replace('.', '/');
	}

	public SortedSet getSourceFiles()
	{
		return new TreeSet(this.children.values());
	}

	public int hashCode()
	{
		return this.name.hashCode();
	}

	public void merge(CoverageData coverageData)
	{
		super.merge(coverageData);

		PackageData packageData = (PackageData)coverageData;
		if (packageData.name != null)
		{
			this.name = packageData.name;
		}
	}

}
