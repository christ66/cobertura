/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 Jiri Mares
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

import java.io.Serializable;

/**
 * <p>
 * This class implements HasBeenInstrumented so that when cobertura instruments
 * itself, it will omit this class. It does this to avoid an infinite recursion
 * problem because instrumented classes make use of this class.
 * </p>
 */
public class JumpData implements BranchCoverageData, Comparable, Serializable,
		HasBeenInstrumented
{
	private static final long serialVersionUID = 3;

	private int branchNumber;

	private long trueHits;

	private long falseHits;

	JumpData(int branchNumber)
	{
		super();
		this.branchNumber = branchNumber;
		this.trueHits = 0L;
		this.falseHits = 0L;
	}

	public int compareTo(Object o)
	{
		if (!o.getClass().equals(JumpData.class))
			return Integer.MAX_VALUE;
		return this.branchNumber - ((JumpData) o).branchNumber;
	}

	void touchBranch(boolean branch)
	{
		if (branch)
		{
			this.trueHits++;
		}
		else
		{
			this.falseHits++;
		}
	}

	public int getBranchNumber()
	{
		return this.branchNumber;
	}

	public long getTrueHits()
	{
		return this.trueHits;
	}

	public long getFalseHits()
	{
		return this.falseHits;
	}

	public double getBranchCoverageRate()
	{
		return ((double) getNumberOfCoveredBranches()) / getNumberOfValidBranches();
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || !(obj.getClass().equals(this.getClass())))
			return false;

		JumpData branchData = (JumpData) obj;
		return (this.trueHits == branchData.trueHits)
				&& (this.falseHits == branchData.falseHits)
				&& (this.branchNumber == branchData.branchNumber);
	}

	public int hashCode()
	{
		return this.branchNumber;
	}

	public int getNumberOfCoveredBranches()
	{
		return ((trueHits > 0) ? 1 : 0) + ((falseHits > 0) ? 1: 0);
	}

	public int getNumberOfValidBranches()
	{
		return 2;
	}

	public void merge(BranchCoverageData coverageData)
	{
		JumpData jumpData = (JumpData) coverageData;
		this.trueHits += jumpData.trueHits;
		this.falseHits += jumpData.falseHits;
	}

}
