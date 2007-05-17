/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
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

import junit.framework.TestCase;

public class LineDataTest extends TestCase
{

	private final LineData a = new LineData(10, "test1", "(I)B");
	private final LineData b = new LineData(11, "test1", "(I)B");
	private final LineData c = new LineData(12, "test2", "(I)B");
	private final LineData d = new LineData(13, "test2", "(I)B");
	private final LineData e = new LineData(14);
	private final LineData f = new LineData(15);

	public void testEquals()
	{
		assertFalse(a.equals(null));
		assertFalse(a.equals(new Integer(4)));

		assertTrue(a.equals(a));
		assertFalse(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(a.equals(d));
		assertFalse(a.equals(e));
		assertFalse(a.equals(f));

		LineData aPrime = new LineData(10, "test1", "(I)B");
		assertTrue(a.equals(aPrime));
	}

	public void testHashCode()
	{
		assertEquals(a.hashCode(), a.hashCode());

		LineData aPrime = new LineData(10, "test1", "(I)B");
		assertEquals(a.hashCode(), aPrime.hashCode());
	}

	public void testGetLineNumber()
	{
		assertEquals(10, a.getLineNumber());
		assertEquals(11, b.getLineNumber());
		assertEquals(12, c.getLineNumber());
		assertEquals(13, d.getLineNumber());
		assertEquals(14, e.getLineNumber());
		assertEquals(15, f.getLineNumber());
	}

	public void testGetNumbers()
	{
		assertEquals(1, a.getBranchCoverageRate(), 0);
		assertEquals(0, a.getLineCoverageRate(), 0);
		assertEquals(0, a.getNumberOfCoveredLines());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(0, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		a.addJump(0);
		a.addJump(1);
		assertEquals(0, a.getBranchCoverageRate(), 0);
		assertEquals(0, a.getLineCoverageRate(), 0);
		assertEquals(0, a.getNumberOfCoveredLines());
		assertEquals(0, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		for (int i = 0; i < 5; i++)
		{
			a.touch();
			assertEquals(0, a.getBranchCoverageRate(), 0);
			assertEquals(1, a.getLineCoverageRate(), 0);
			assertEquals(1, a.getNumberOfCoveredLines());
			assertEquals(0, a.getNumberOfCoveredBranches());
			assertEquals(4, a.getNumberOfValidBranches());
			assertEquals(1, a.getNumberOfValidLines());
		}

		a.touchJump(0, true);
		assertEquals(0.25, a.getBranchCoverageRate(), 0);
		assertEquals(1, a.getLineCoverageRate(), 0);
		assertEquals(1, a.getNumberOfCoveredLines());
		assertEquals(1, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		a.touchJump(1, false);
		assertEquals(0.5, a.getBranchCoverageRate(), 0);
		assertEquals(1, a.getLineCoverageRate(), 0);
		assertEquals(1, a.getNumberOfCoveredLines());
		assertEquals(2, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		a.touchJump(1, true);
		assertEquals(0.75, a.getBranchCoverageRate(), 0);
		assertEquals(1, a.getLineCoverageRate(), 0);
		assertEquals(1, a.getNumberOfCoveredLines());
		assertEquals(3, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());

		a.touchJump(0, false);
		assertEquals(1, a.getBranchCoverageRate(), 0);
		assertEquals(1, a.getLineCoverageRate(), 0);
		assertEquals(1, a.getNumberOfCoveredLines());
		assertEquals(4, a.getNumberOfCoveredBranches());
		assertEquals(4, a.getNumberOfValidBranches());
		assertEquals(1, a.getNumberOfValidLines());
	}

	public void testSetConditional()
	{
		assertFalse(c.hasBranch());
		c.addJump(0);
		assertTrue(c.hasBranch());
		c.addJump(1);
		assertTrue(c.hasBranch());
	}

	public void testSetMethodNameAndDescriptor()
	{
		e.setMethodNameAndDescriptor("test3", "(I)B");
		assertEquals("test3", e.getMethodName());
		assertEquals("(I)B", e.getMethodDescriptor());

		f.setMethodNameAndDescriptor("test4", "(I)B");
		assertEquals("test4", f.getMethodName());
		assertEquals("(I)B", f.getMethodDescriptor());
	}

	public void testTouch()
	{
		assertEquals(0, a.getHits());
		for (int i = 0; i < 400; i++)
			a.touch();
		assertEquals(400, a.getHits());
	}

}
