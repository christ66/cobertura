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

import java.io.Serializable;
import java.util.Set;

/**
 * CoverageData information is typically serialized to a file. An
 * instance of this class records coverage information for a single
 * class that has been instrumented.
 */
public interface CoverageData extends Serializable
{

	/**
	 * Default file name used to write instrumentation information.
	 */
	String FILE_NAME = "cobertura.ser";

	/**
	 * @return The branch coverage rate for the class.
	 */
	double getBranchCoverageRate();

	/**
	 * @return The branch coverage rate for a particular method.
	 */
	double getBranchCoverageRate(String methodNameAndSignature);

	/**
	 * @param lineNumber The source code line number.
	 * @return The number of hits a particular line of code has.
	 */
	long getHitCount(int lineNumber);

	/**
	 * @return The line coverage rate for the class
	 */
	double getLineCoverageRate();

	/**
	 * @return The line coverage rate for particular method
	 */
	double getLineCoverageRate(String methodNameAndSignature);

	/**
	 * @return The method name and signature of each method found in the
	 * class represented by this instrumentation.
	 */
	Set getMethodNamesAndSignatures();

	/**
	 * @return The number of branches in this class.
	 */
	int getNumberOfBranches();

	/**
	 * @return The number of branches in this class covered by testing.
	 */
	int getNumberOfCoveredBranches();

	/**
	 * @return The number of lines in this class covered by testing.
	 */
	int getNumberOfCoveredLines();

	/**
	 * @return The number of lines in this class.
	 */
	int getNumberOfLines();

	/**
	 * @return The source file name.
	 */
	String getSourceFileName();

	/**
	 * @return The set of valid source line numbers
	 */
	Set getSourceLineNumbers();

	/**
	 * Determine if a given line number is a valid line of code.
	 *
	 * @return True if the line contains executable code.  False
	 *         if the line is empty, or a comment, etc.
	 */
	boolean isValidSourceLineNumber(int lineNumber);

	/**
	 * Merge some existing instrumentation with this instrumentation.
	 *
	 * @param coverageData Some existing coverage data.
	 */
	void merge(CoverageData coverageData);

	/**
	 * Increment the number of hits for a particular line of code.
	 *
	 * @param lineNumber the line of code to increment the number of hits.
	 */
	void touch(int lineNumber);
}