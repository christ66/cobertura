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

package net.sourceforge.cobertura.reporting.html;

import java.util.Set;

import junit.framework.TestCase;

public class CoverageTest extends TestCase
{

	private Clazz clazz1;
	private Clazz clazz2;
	private Clazz clazz3;

	public void setUp()
	{
		clazz1 = new Clazz("HelloWorld");
		clazz1.setNumberOfBranches(4);
		clazz1.setNumberOfCoveredBranches(2);
		clazz1.setNumberOfCoveredLines(20);
		clazz1.setNumberOfLines(40);
		for (int i = 0; i < clazz1.getNumberOfLines(); i++)
		{
			clazz1.addLine(i + 1, 5 * (i + 1));
		}

		clazz2 = new Clazz("com.example.HelloWorld");
		clazz2.setNumberOfBranches(6);
		clazz2.setNumberOfCoveredBranches(3);
		clazz2.setNumberOfCoveredLines(10);
		clazz2.setNumberOfLines(20);
		for (int i = 0; i < clazz2.getNumberOfLines(); i++)
		{
			clazz1.addLine(i + 1, 4 * (i + 1));
		}

		clazz3 = new Clazz("com.example.GoodbyeWorld");
		clazz3.setNumberOfBranches(8);
		clazz3.setNumberOfCoveredBranches(4);
		clazz3.setNumberOfCoveredLines(5);
		clazz3.setNumberOfLines(10);
		for (int i = 0; i < clazz3.getNumberOfLines(); i++)
		{
			clazz1.addLine(i + 1, 9 * (i + 1));
		}
	}

	public void tearDown()
	{
		clazz1 = null;
		clazz2 = null;
		clazz3 = null;
	}

	public void testCoverage()
	{
		Coverage coverage = new Coverage();

		assertEquals(0, coverage.getNumberOfBranches());
		assertEquals(0, coverage.getNumberOfLines());
		assertEquals(0, coverage.getBranchCoverageRate(), 0.00001);
		assertEquals(1, coverage.getLineCoverageRate(), 0.00001);
		assertNotNull(coverage.getPackages());
		assertEquals(0, coverage.getPackages().size());
		assertNotNull(coverage.getClasses());
		assertEquals(0, coverage.getClasses().size());

		coverage.addClass(clazz1);

		assertEquals(clazz1.getNumberOfBranches(), coverage
				.getNumberOfBranches());
		assertEquals(clazz1.getNumberOfLines(), coverage.getNumberOfLines());
		assertEquals(clazz1.getBranchCoverageRate(), coverage
				.getBranchCoverageRate(), 0.00001);
		assertEquals(clazz1.getLineCoverageRate(), coverage
				.getLineCoverageRate(), 0.00001);
		assertNotNull(coverage.getPackages());
		assertEquals(1, coverage.getPackages().size());
		assertNotNull(coverage.getClasses());
		assertEquals(1, coverage.getClasses().size());

		coverage.addClass(clazz2);

		assertEquals(clazz1.getNumberOfBranches()
				+ clazz2.getNumberOfBranches(), coverage
				.getNumberOfBranches());
		assertEquals(clazz1.getNumberOfLines() + clazz2.getNumberOfLines(),
				coverage.getNumberOfLines());
		assertEquals(0.5, coverage.getBranchCoverageRate(), 0.00001);
		assertEquals(0.5, coverage.getLineCoverageRate(), 0.00001);
		assertNotNull(coverage.getPackages());
		assertEquals(2, coverage.getPackages().size());
		assertNotNull(coverage.getClasses());
		assertEquals(2, coverage.getClasses().size());

		coverage.addClass(clazz3);

		assertEquals(
				clazz1.getNumberOfBranches() + clazz2.getNumberOfBranches()
						+ clazz3.getNumberOfBranches(), coverage
						.getNumberOfBranches());
		assertEquals(clazz1.getNumberOfLines() + clazz2.getNumberOfLines()
				+ clazz3.getNumberOfLines(), coverage.getNumberOfLines());
		assertEquals(0.5, coverage.getBranchCoverageRate(), 0.00001);
		assertEquals(0.5, coverage.getLineCoverageRate(), 0.00001);
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

		assertEquals(clazz1.getNumberOfBranches()
				+ clazz2.getNumberOfBranches(), coverage
				.getNumberOfBranches());
		assertEquals(clazz1.getNumberOfLines() + clazz2.getNumberOfLines(),
				coverage.getNumberOfLines());
		assertEquals(0.5, coverage.getBranchCoverageRate(), 0.00001);
		assertEquals(0.5, coverage.getLineCoverageRate(), 0.00001);
		assertNotNull(coverage.getPackages());
		assertEquals(2, coverage.getPackages().size());
		assertNotNull(coverage.getClasses());
		assertEquals(2, coverage.getClasses().size());

	}
}