/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

package net.sourceforge.cobertura.reporting;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javancss.Javancss;
import javancss.JavancssConstants;

public abstract class Util
{

	/**
	 * Calculates the code complexity number for a given class.
	 * "CCN" stands for "code complexity number."  This is
	 * sometimes referred to as McCabe's number.  This method
	 * calculates the average cyclomatic code complexity of all
	 * methods in a given class, or of all methods of all classes
	 * in a given directory.  This recursive calculation is useful
	 * for calculating the average CCN for a specific package.
	 *
	 * @param file The source of a Java class for which you want
	 *        to calculate the complexity, or a directory containing
	 *        Java source files for which you want to calculate the
	 *        complexity.
	 * @param recursive If file is a directory, this parameter is
	 *        used to decide whether to only list the files in the
	 *        given directory, or to list all files in all 
	 *        subdirectories of the given directory.
	 * @return The average cyclomatic code complexity for this class.
	 */
	public static double getCCN(File file, boolean recursive)
	{
		int ccnAccumulator = 0;

		Vector files = getListOfFiles(file, recursive);
		Javancss javancss = new Javancss(files);

		List functionMetrics = javancss.getFunctionMetrics();
		if (functionMetrics.size() <= 0)
			return 0;

		Iterator iter = functionMetrics.iterator();
		while (iter.hasNext())
		{
			Vector functionMetric = (Vector)iter.next();
			ccnAccumulator += ((Integer)functionMetric
					.elementAt(JavancssConstants.FCT_CCN)).intValue();
		}

		return (double)ccnAccumulator / functionMetrics.size();
	}

	/**
	 * Create a <code>Vector</code> containing the file names of
	 * Java source code.  If the given file parameter is a regular
	 * file, then the return value only contains the absolute
	 * path of this file.  However, if the given file parameter
	 * is a directory, this vector contains absolute paths to all
	 * files under this directory and all subdirectories.
	 *
	 * @param file A Java source file or a directory containing
	 *        Java source files.
	 * @return A Vector containing <code>String</code>s that
	 *         are absolute paths to Java source files.
	 */
	private static Vector getListOfFiles(File file, boolean recursive)
	{
		Vector ret = new Vector();

		if (file.isFile())
		{
			ret.add(file.getAbsolutePath());
		}
		else if (file.isDirectory())
		{
		    File[] files = file.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getAbsolutePath().endsWith(".java");
                }
			});
		    
			for (int i = 0; i < files.length; i++)
			{
				if (recursive)
				{
					ret.addAll(getListOfFiles(files[i], true));
				}
				else
				{
					if (files[i].isFile())
					{
						ret.add(files[i].getAbsolutePath());
					}
				}
			}
		}

		return ret;
	}

}