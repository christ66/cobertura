/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Grzegorz Lukasik
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ProjectData extends CoverageDataContainer
		implements HasBeenInstrumented
{

	private static final long serialVersionUID = 6;

	private static ProjectData globalProjectData = null;

	private static SaveTimer saveTimer = null;

	/** This collection is used for quicker access to the list of source files. */
	private Map sourceFiles = new HashMap();

	/** This collection is used for quicker access to the list of classes. */
	private Map classes = new HashMap();

	public void addClassData(ClassData classData)
	{
		String packageName = classData.getPackageName();
		PackageData packageData = (PackageData)children.get(packageName);
		if (packageData == null)
		{
			packageData = new PackageData(packageName);
			// Each key is a package name, stored as an String object.
			// Each value is information about the package, stored as a PackageData object.
			this.children.put(packageName, packageData);
		}
		packageData.addClassData(classData);
		this.sourceFiles.put(classData.getSourceFileName(), packageData.getChild(classData.getSourceFileName()));
		this.classes.put(classData.getName(), classData);
	}

	public ClassData getClassData(String name)
	{
		return (ClassData)this.classes.get(name);
	}

	public ClassData getOrCreateClassData(String name)
	{
		ClassData classData = (ClassData)this.classes.get(name);
		if (classData == null)
		{
			classData = new ClassData(name);
			addClassData(classData);
		}
		return classData;
	}

	public Collection getClasses()
	{
		return this.classes.values();
	}

	public int getNumberOfClasses()
	{
		return this.classes.size();
	}

	public int getNumberOfSourceFiles()
	{
		return this.sourceFiles.size();
	}

	public SortedSet getPackages()
	{
		return new TreeSet(this.children.values());
	}

	public Collection getSourceFiles()
	{
		return this.sourceFiles.values();
	}

	/**
	 * Get all subpackages of the given package.
	 *
	 * @param packageName The package name to find subpackages for.
	 *        For example, "com.example"
	 * @return A collection containing PackageData objects.  Each one
	 *         has a name beginning with the given packageName.  For
	 *         example, "com.example.io"
	 */
	public SortedSet getSubPackages(String packageName)
	{
		SortedSet subPackages = new TreeSet();
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			PackageData packageData = (PackageData)iter.next();
			if (packageData.getName().startsWith(packageName))
				subPackages.add(packageData);
		}
		return subPackages;
	}

	public void merge(CoverageData coverageData)
	{
		super.merge(coverageData);

		ProjectData projectData = (ProjectData)coverageData;
		for (Iterator iter = projectData.classes.keySet().iterator(); iter.hasNext();)
		{
			Object key = iter.next();
			if (!this.classes.containsKey(key))
			{
				this.classes.put(key, projectData.classes.get(key));
			}
		}
	}

	public static ProjectData getGlobalProjectData()
	{
		if (globalProjectData != null)
			return globalProjectData;

		File dataFile = CoverageDataFileHandler.getDefaultDataFile();

		// Read projectData from the serialized file.
		if (dataFile.isFile())
		{
			//System.out.println("Cobertura: Loading global project data from " + dataFile.getAbsolutePath());
			globalProjectData = CoverageDataFileHandler
					.loadCoverageData(dataFile);
		}

		if (globalProjectData == null)
		{
			// We could not read from the serialized file, so create a new object.
			System.out.println("Cobertura: Coverage data file "
							+ dataFile.getAbsolutePath()
							+ " either does not exist or is not readable.  Creating a new data file.");
			globalProjectData = new ProjectData();
		}

		// Hack for Tomcat - by saving project data right now we force loading
		// of classes involved in this process (like ObjectOutputStream)
		// so that it won't be necessary to load them on JVM shutdown
		if( System.getProperty("catalina.home")!=null) {
			saveGlobalProjectData();
			
			// Additionaly force loading of other classes that might be not loaded
			// becouse saved project data was empty
			ClassData.class.toString();
			CoverageData.class.toString();
			CoverageDataContainer.class.toString();
			HasBeenInstrumented.class.toString();
			LineData.class.toString();
			PackageData.class.toString();
			SourceFileData.class.toString();
		}
		
		// Add a hook to save the data when the JVM exits
		saveTimer = new SaveTimer();
		Runtime.getRuntime().addShutdownHook(new Thread(saveTimer));

		// Possibly also save the coverage data every x seconds?
		//Timer timer = new Timer(true);
		//timer.schedule(saveTimer, 100);

		return globalProjectData;
	}

	public static void saveGlobalProjectData()
	{
		ProjectData projectData = getGlobalProjectData();
		synchronized (projectData)
		{
			CoverageDataFileHandler.saveCoverageData(projectData,
					CoverageDataFileHandler.getDefaultDataFile());
		}
	}

}
