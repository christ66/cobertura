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

package net.sourceforge.cobertura.reporting.html;

import java.util.Iterator;
import java.util.Map;

import net.sourceforge.cobertura.coverage.CoverageData;
import net.sourceforge.cobertura.reporting.Util;

public abstract class CoverageUnmarshaller
{

	public static Coverage parse(Map coverageData) throws Exception
	{
		Coverage coverage = new Coverage();

		Iterator iter = coverageData.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry)iter.next();
			String classname = (String)entry.getKey();
			CoverageData instrumentation = (CoverageData)entry.getValue();
			coverage.addClass(parseClass(classname, instrumentation));
		}

		return coverage;
	}

	private static Clazz parseClass(String classname,
			CoverageData instrumentation)
	{
		Clazz clazz = new Clazz(classname);

		clazz.setNumberOfBranches(instrumentation.getNumberOfBranches());
		clazz.setNumberOfCoveredLines(instrumentation
				.getNumberOfCoveredLines());
		clazz.setNumberOfCoveredBranches(instrumentation
				.getNumberOfCoveredBranches());
		clazz.setNumberOfLines(instrumentation.getNumberOfLines());

		Iterator iter = instrumentation.getSourceLineNumbers().iterator();
		while (iter.hasNext())
		{
			int lineNumber = ((Integer)iter.next()).intValue();
			long numberOfHits = instrumentation.getHitCount(lineNumber);
			clazz.addLine(lineNumber, numberOfHits);
		}

		return clazz;
	}

}
