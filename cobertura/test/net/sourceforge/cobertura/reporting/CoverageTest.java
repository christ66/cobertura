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

package net.sourceforge.cobertura.reporting;

import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coverage.CoverageData;

public class CoverageTest extends TestCase
{

	private Clazz clazz1;
	private Clazz clazz2;
	private Clazz clazz3;

	private CoverageData getTestDataInstance(int numberOfLines, int linesTouched, int[] conditionalLines) {
	    CoverageData data = new CoverageData();
	    for (int i = 1; i <= numberOfLines; i++) {
	        data.addLine(i, "testMethod", "(I)B");
	    }
	    
	    for (int i = 0; i < conditionalLines.length; i++) {
	        data.markLineAsConditional(conditionalLines[i]);
	    }
	    
	    for (int i = 1; i <= linesTouched; i++) {
	        data.touch(i);
	    }
	    return data;
	}
	
	public void setUp()
	{
	    // clazz1 expects 50% line coverage && 100% branch coverage 
	    clazz1 = new Clazz("HelloWorld", getTestDataInstance(40, 20, new int[] { 5, 10, 15, 18 }));
	    // clazz2 expects 50% line coverage && 66% branch coverage
	    clazz2 = new Clazz("com.example.HelloWorld", getTestDataInstance(20, 10, new int[] { 4, 7, 15 }));
	    // clazz3 expects 40% line coverage && 33% branch coverage
	    clazz3 = new Clazz("com.example.GoodbyeWorld", getTestDataInstance(10, 4, new int[] { 3, 7, 9 }));
	}

	public void tearDown()
	{
		clazz1 = null;
		clazz2 = null;
		clazz3 = null;
	}

	public void testCoverage()
	{
		CoverageReport coverage = new CoverageReport(Collections.EMPTY_MAP);

		assertEquals(0, coverage.getNumberOfBranches());
		assertEquals(0, coverage.getNumberOfLines());
		assertEquals(0, coverage.getBranchCoverageRate(), 0.00001);
		assertEquals(1, coverage.getLineCoverageRate(), 0.00001);
		assertNotNull(coverage.getPackages());
		assertEquals(0, coverage.getPackages().size());
		assertNotNull(coverage.getClasses());
		assertEquals(0, coverage.getClasses().size());

		coverage.addClass(clazz1);

		assertEquals(clazz1.getNumberOfBranches(), coverage.getNumberOfBranches());
		assertEquals(clazz1.getNumberOfLines(), coverage.getNumberOfLines());
		assertEquals(clazz1.getBranchCoverageRate(), coverage.getBranchCoverageRate(), 0.00001);
		assertEquals(clazz1.getLineCoverageRate(), coverage.getLineCoverageRate(), 0.00001);
		assertNotNull(coverage.getPackages());
		assertEquals(1, coverage.getPackages().size());
		assertNotNull(coverage.getClasses());
		assertEquals(1, coverage.getClasses().size());

		coverage.addClass(clazz2);

		assertEquals(clazz1.getNumberOfBranches() + clazz2.getNumberOfBranches(), coverage.getNumberOfBranches());
		assertEquals(clazz1.getNumberOfLines() + clazz2.getNumberOfLines(), coverage.getNumberOfLines());
		assertEquals(0.85, coverage.getBranchCoverageRate(), 0.008);
		assertEquals(0.5, coverage.getLineCoverageRate(), 0.00001);
		assertNotNull(coverage.getPackages());
		assertEquals(2, coverage.getPackages().size());
		assertNotNull(coverage.getClasses());
		assertEquals(2, coverage.getClasses().size());

		coverage.addClass(clazz3);

		assertEquals(
				clazz1.getNumberOfBranches() + clazz2.getNumberOfBranches() + clazz3.getNumberOfBranches(), 
				coverage.getNumberOfBranches()
				);

		assertEquals(
		        clazz1.getNumberOfLines() + clazz2.getNumberOfLines() + clazz3.getNumberOfLines(), 
		        coverage.getNumberOfLines()
		        );
		
		assertEquals(0.7, coverage.getBranchCoverageRate(), 0.00001);
		assertEquals(0.4857, coverage.getLineCoverageRate(), 0.001);
		assertNotNull(coverage.getPackages());
		assertEquals(2, coverage.getPackages().size());
		assertNotNull(coverage.getClasses());
		assertEquals(3, coverage.getClasses().size());

		try
		{
			coverage.addClass(clazz3);
			fail("Expected an IllegalArgumentException but did not receive one!");
		}
		catch (IllegalArgumentException e)
		{
			//Good
		}

		Set packagesSet = coverage.getPackages();
		Package[] packages = (Package[])packagesSet
				.toArray(new Package[packagesSet.size()]);
		assertEquals("", packages[0].getName());
		assertEquals(1, coverage.getSubPackages(packages[0]).size());
		assertEquals("com.example", packages[1].getName());
		assertEquals(0, coverage.getSubPackages(packages[1]).size());

		coverage.removeClass(clazz3);


		assertEquals(clazz1.getNumberOfBranches() + clazz2.getNumberOfBranches(), coverage.getNumberOfBranches());
		assertEquals(clazz1.getNumberOfLines() + clazz2.getNumberOfLines(), coverage.getNumberOfLines());
		assertEquals(0.85, coverage.getBranchCoverageRate(), 0.008);
		assertEquals(0.5, coverage.getLineCoverageRate(), 0.00001);
		assertNotNull(coverage.getPackages());
		assertEquals(2, coverage.getPackages().size());
		assertNotNull(coverage.getClasses());
		assertEquals(2, coverage.getClasses().size());

	}
}