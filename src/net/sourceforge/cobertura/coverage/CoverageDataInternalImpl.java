/**
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

package net.sourceforge.cobertura.coverage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

class CoverageDataInternalImpl
		implements CoverageDataInternal, HasBeenInstrumented
{

	private static final long serialVersionUID = 1;

	private static final transient Logger logger = Logger
			.getLogger(CoverageDataInternalImpl.class);

	private String sourceFileName;
	private final Set methodNamesAndSignatures = new HashSet();
	private final Map sourceLineNumbers = new HashMap();
	private final Map sourceLineNumbersByMethod = new HashMap();
	private final Set conditionals = new HashSet();
	private final Map conditionalsByMethod = new HashMap();

	public String getSourceFileName()
	{
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName)
	{
		this.sourceFileName = sourceFileName;
	}

	public Set getMethodNamesAndSignatures()
	{
		return methodNamesAndSignatures;
	}

	public void setMethodNamesAndSignatures(Set methodNamesAndSignatures)
	{
		this.methodNamesAndSignatures.addAll(methodNamesAndSignatures);
	}

	public long getHitCount(int lineNumber)
	{
		return getHitCount(new Integer(lineNumber));
	}

	private long getHitCount(Integer lineNumber)
	{
		if (!sourceLineNumbers.containsKey(lineNumber))
		{
			return 0;
		}

		return ((Long)sourceLineNumbers.get(lineNumber)).longValue();
	}

	private void setHitCount(int lineNumber, long lineCount)
	{
		sourceLineNumbers.put(new Integer(lineNumber), new Long(lineCount));
	}

	private void setHitCount(Integer lineNumber, long lineCount)
	{
		sourceLineNumbers.put(lineNumber, new Long(lineCount));
	}

	public Set getSourceLineNumbers()
	{
		return Collections.unmodifiableSet(sourceLineNumbers.keySet());
	}

	public void setSourceLineNumbers(Set sourceLineNumbers)
	{
		Iterator iter = sourceLineNumbers.iterator();
		while (iter.hasNext())
		{
			this.sourceLineNumbers.put(iter.next(), new Long(0));
		}
	}

	public void setSourceLineNumbersByMethod(Map sourceLineNumbersByMethod)
	{
		this.sourceLineNumbersByMethod.putAll(sourceLineNumbersByMethod);
	}

	public Set getConditionals()
	{
		return conditionals;
	}

	public void setConditionals(Set conditionals)
	{
		this.conditionals.addAll(conditionals);
	}

	public void setConditionalsByMethod(Map conditionalsByMethod)
	{
		this.conditionalsByMethod.putAll(conditionalsByMethod);
	}

	public void touch(int lineNumber)
	{
		Integer key = new Integer(lineNumber);
		setHitCount(key, getHitCount(key) + 1);
	}

	public void merge(CoverageData coverageData)
	{
		/**
		 * Give the new source file name precedence, I guess...
		 */
		if (getSourceFileName() == null)
		{
			setSourceFileName(coverageData.getSourceFileName());
		}

		methodNamesAndSignatures.addAll(coverageData
				.getMethodNamesAndSignatures());
		Iterator i = coverageData.getSourceLineNumbers().iterator();
		while (i.hasNext())
		{
			int lineNumber = ((Integer)i.next()).intValue();
			long hitCount = getHitCount(lineNumber)
					+ coverageData.getHitCount(lineNumber);
			setHitCount(lineNumber, hitCount);
		}

		if (!(coverageData instanceof CoverageDataInternalImpl))
		{
			return;
		}

		CoverageDataInternalImpl coverageDataInternalImpl = (CoverageDataInternalImpl)coverageData;
		sourceLineNumbersByMethod
				.putAll(coverageDataInternalImpl.sourceLineNumbersByMethod);
		conditionals.addAll(coverageDataInternalImpl.conditionals);
		conditionalsByMethod
				.putAll(coverageDataInternalImpl.conditionalsByMethod);
	}

	public double getBranchCoverageRate()
	{
		if (conditionals.size() == 0)
		{
			// no conditional branches, therefore 100% branch coverage.
			return 1d;
		}
		int hits = 0;
		Iterator iter = conditionals.iterator();
		while (iter.hasNext())
		{
			Conditional next = (Conditional)iter.next();
			int lineNumber = next.getLineNumber();
			if (getHitCount(lineNumber) > 0)
			{
				hits++;
			}
		}
		return (double)hits / conditionals.size();
	}

	public double getBranchCoverageRate(String methodNameAndSignature)
	{
		if (!sourceLineNumbersByMethod.containsKey(methodNameAndSignature))
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("sourceLineNumbersByMethod: "
						+ sourceLineNumbersByMethod.keySet());
			}

			throw new IllegalArgumentException(methodNameAndSignature);
		}

		Set methodConditionals = (Set)conditionalsByMethod
				.get(methodNameAndSignature);
		if (methodConditionals.size() == 0)
		{
			// no conditional branches, therefore 100% branch coverage.
			return 1d;
		}

		int hits = 0;
		Iterator iter = methodConditionals.iterator();
		while (iter.hasNext())
		{
			Conditional next = (Conditional)iter.next();
			int lineNumber = next.getLineNumber();
			if (getHitCount(lineNumber) > 0)
			{
				hits++;
			}
		}
		return (double)hits / methodConditionals.size();
	}

	public double getLineCoverageRate()
	{
		if (sourceLineNumbers.size() == 0)
		{
			return 1d;
		}
		int hits = 0;
		Iterator iter = sourceLineNumbers.values().iterator();
		while (iter.hasNext())
		{
			if (((Long)iter.next()).longValue() > 0)
				hits++;
		}
		return (double)hits / sourceLineNumbers.size();
	}

	public double getLineCoverageRate(String methodNameAndSignature)
	{
		if (!sourceLineNumbersByMethod.containsKey(methodNameAndSignature))
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("sourceLineNumbersByMethod: "
						+ sourceLineNumbersByMethod.keySet());
			}

			throw new IllegalArgumentException(methodNameAndSignature);
		}

		Set lineNumbers = (Set)sourceLineNumbersByMethod
				.get(methodNameAndSignature);
		if (lineNumbers.size() == 0)
		{
			return 1d;
		}

		int count = 0;
		Iterator i = lineNumbers.iterator();
		while (i.hasNext())
		{
			if (getHitCount((Integer)i.next()) > 0)
			{
				count++;
			}
		}

		return ((double)count) / ((double)lineNumbers.size());
	}

	public int getNumberOfBranches()
	{
		return conditionals.size();
	}

	public int getNumberOfCoveredBranches()
	{
		int hits = 0;

		Iterator iter = conditionals.iterator();
		while (iter.hasNext())
		{
			Conditional next = (Conditional)iter.next();
			int lineNumber = next.getLineNumber();
			if (getHitCount(lineNumber) > 0)
			{
				hits++;
			}
		}

		return hits;
	}

	public int getNumberOfCoveredLines()
	{
		int hits = 0;

		Iterator iter = sourceLineNumbers.values().iterator();
		while (iter.hasNext())
		{
			if (((Long)iter.next()).longValue() > 0)
				hits++;
		}

		return hits;
	}

	public int getNumberOfLines()
	{
		return sourceLineNumbers.size();
	}

	public boolean isValidSourceLineNumber(int lineNumber)
	{
		return sourceLineNumbers.containsKey(new Integer(lineNumber));
	}
}