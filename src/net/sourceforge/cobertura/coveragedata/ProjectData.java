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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class ProjectData extends CoverageDataContainer
		implements HasBeenInstrumented
{

	private static final long serialVersionUID = 4;

	private static final Logger LOGGER = Logger.getLogger(ProjectData.class);

	private static ProjectData globalProjectData = null;

	private static SaveTimer saveTimer = null;

	private Map classes = new HashMap();

	public ProjectData()
	{
	}

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

	public SortedSet getSourceFiles()
	{
		SortedSet sourceFiles = new TreeSet();

		Iterator iter = this.children.values().iterator();
		while (iter.hasNext()) {
			PackageData packageData = (PackageData)iter.next();
			sourceFiles.addAll(packageData.getChildren());
		}

		return sourceFiles;
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
	public Collection getSubPackages(String packageName)
	{
		Collection subPackages = new HashSet();
		Iterator iter = this.children.values().iterator();
		while (iter.hasNext())
		{
			PackageData packageData = (PackageData)iter.next();
			if (packageData.getName().equals(packageName))
				subPackages.add(packageData);
		}
		return subPackages;
	}

	public static ProjectData getGlobalProjectData()
	{
		if (saveTimer == null)
		{
			saveTimer = new SaveTimer();
			Runtime.getRuntime().addShutdownHook(new Thread(saveTimer));
			//Timer timer = new Timer(true);
			//timer.schedule(saveTimer, 100);
		}

		if (globalProjectData != null)
			return globalProjectData;

		File dataFile = CoverageDataFileHandler.getDefaultDataFile();

		// Read projectData from the serialized file.
		if (dataFile.isFile())
		{
			LOGGER.debug("Loading global project data from "
					+ dataFile.getAbsolutePath());
			globalProjectData = CoverageDataFileHandler
					.loadCoverageData(dataFile);
		}
		if (globalProjectData != null)
			return globalProjectData;

		// We could not read from the serialized file, so create a new object.
		LOGGER
				.info("Coverage data file "
						+ dataFile.getAbsolutePath()
						+ " either does not exist or is not readable.  Creating a new data file.");
		globalProjectData = new ProjectData();

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
