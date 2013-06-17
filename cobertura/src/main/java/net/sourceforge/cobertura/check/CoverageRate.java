/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
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

package net.sourceforge.cobertura.check;

public class CoverageRate {

	private final double lineCoverageRate;
	private final double branchCoverageRate;

	public CoverageRate(double lineCoverageRate, double branchCoverageRate) {
		this.lineCoverageRate = lineCoverageRate;
		this.branchCoverageRate = branchCoverageRate;
	}

	public double getLineCoverageRate() {
		return lineCoverageRate;
	}

	public double getBranchCoverageRate() {
		return branchCoverageRate;
	}
}