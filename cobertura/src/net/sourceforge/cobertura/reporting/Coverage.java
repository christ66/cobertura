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

package net.sourceforge.cobertura.reporting;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Coverage
{

	private Map packages;

	public Coverage()
	{
		packages = new TreeMap();
	}

	public void addClass(Clazz clazz)
	{
		String packageName = clazz.getPackageName();
		Package pkg = (Package)packages.get(packageName);
		if (pkg == null)
		{
			pkg = new Package(packageName);
			packages.put(packageName, pkg);
		}
		pkg.addClass(clazz);
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
		Set ret = new TreeSet();

		Iterator iter = packages.values().iterator();
		while (iter.hasNext())
		{
			Package pkg = (Package)iter.next();
			ret.addAll(pkg.getClasses());
		}

		return ret;
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

	public long getNumberOfBranches()
	{
		long numberOfBranches = 0;

		Iterator iter = packages.values().iterator();
		while (iter.hasNext())
		{
			Package pkg = (Package)iter.next();
			numberOfBranches += pkg.getNumberOfBranches();
		}

		return numberOfBranches;
	}

	public int getNumberOfClasses()
	{
		int numberOfClasses = 0;

		Iterator iter = packages.values().iterator();
		while (iter.hasNext())
		{
			Package pkg = (Package)iter.next();
			numberOfClasses += pkg.getClasses().size();
		}

		return numberOfClasses;
	}

	public long getNumberOfCoveredBranches()
	{
		long numberOfCoveredBranches = 0;

		Iterator iter = packages.values().iterator();
		while (iter.hasNext())
		{
			Package pkg = (Package)iter.next();
			numberOfCoveredBranches += pkg.getNumberOfCoveredBranches();
		}

		return numberOfCoveredBranches;
	}

	public long getNumberOfCoveredLines()
	{
		long numberOfCoveredLines = 0;

		Iterator iter = packages.values().iterator();
		while (iter.hasNext())
		{
			Package pkg = (Package)iter.next();
			numberOfCoveredLines += pkg.getNumberOfCoveredLines();
		}

		return numberOfCoveredLines;
	}

	public long getNumberOfLines()
	{
		long numberOfLines = 0;

		Iterator iter = packages.values().iterator();
		while (iter.hasNext())
		{
			Package pkg = (Package)iter.next();
			numberOfLines += pkg.getNumberOfLines();
		}

		return numberOfLines;
	}

	public Set getPackages()
	{
		return new TreeSet(packages.values());
	}

	public Set getSubPackages(Package pkg)
	{
		Set ret = new TreeSet();

		Iterator iter = packages.values().iterator();
		while (iter.hasNext())
		{
			Package nextPkg = (Package)iter.next();
			if (nextPkg.getName().startsWith(pkg.getName())
					&& !nextPkg.getName().equals(pkg.getName()))
			{
				ret.add(nextPkg);
			}
		}

		return ret;
	}

	public void removeClass(Clazz clazz)
	{
		String packageName = clazz.getPackageName();
		Package pkg = (Package)packages.get(packageName);
		pkg.removeClass(clazz);
		if (pkg.getClasses().size() == 0)
		{
			packages.remove(packageName);
		}
	}

}
