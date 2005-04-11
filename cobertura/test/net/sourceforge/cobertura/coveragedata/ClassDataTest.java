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

package net.sourceforge.cobertura.coveragedata;

import junit.framework.TestCase;

public class ClassDataTest extends TestCase
{

	private final ClassData a = new ClassData("HelloWorld");
	private final ClassData b = new ClassData("HelloWorld");
	private final ClassData c = new ClassData("HelloWorld");

	public void setUp()
	{
		a.setSourceFileName("com/example/HelloWorld.java");
		b.setSourceFileName("com/example/HelloWorld.java");
		c.setSourceFileName("com/example/HelloWorld.java");

		for (int i = 1; i < 5; i++)
			b.addLine(i, "test", "(I)B");
		for (int i = 1; i < 5; i++)
			c.addLine(i, "test", "(I)B");

		b.touch(1);
		b.touch(2);
	}

	public void testBranch()
	{
		// Setting an invalid line as a branch should not make the line valid
		assertFalse(a.isBranch(2));
		a.markLineAsBranch(2);
		assertFalse(a.isBranch(2));

		assertFalse(b.isBranch(2));
		b.markLineAsBranch(2);
		assertTrue(b.isBranch(2));
	}

	public void testBranchCoverageRate()
	{
		assertEquals(1.00d, a.getBranchCoverageRate(), 0d);
		assertEquals(1.00d, b.getBranchCoverageRate(), 0d);

		assertEquals(1.00d, a.getBranchCoverageRate("test(I)B"), 0d);
		assertEquals(1.00d, b.getBranchCoverageRate("test(I)B"), 0d);

		c.markLineAsBranch(1);
		c.markLineAsBranch(2);
		c.markLineAsBranch(3);
		c.markLineAsBranch(4);

		assertEquals(0.00d, c.getBranchCoverageRate(), 0d);
		assertEquals(0.00d, c.getBranchCoverageRate("test(I)B"), 0d);

		c.touch(1);
		c.touch(2);

		assertEquals(0.50d, c.getBranchCoverageRate(), 0d);
		assertEquals(0.50d, c.getBranchCoverageRate("test(I)B"), 0d);
	}

	public void testEquals()
	{
		assertTrue(a.equals(a));
		assertFalse(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(b.equals(a));
		assertTrue(b.equals(b));
		assertFalse(b.equals(c));
		assertFalse(c.equals(a));
		assertFalse(c.equals(b));
		assertTrue(c.equals(c));

		c.touch(1);
		c.touch(2);
		assertTrue(b.equals(c));
	}

	public void testGetHitCount()
	{
		assertEquals(0, a.getHitCount(45));
		assertEquals(0, b.getHitCount(45));
		assertEquals(0, c.getHitCount(-45));

		assertEquals(1, b.getHitCount(1));
		assertEquals(1, b.getHitCount(2));
		assertEquals(0, b.getHitCount(3));

		assertEquals(0, c.getHitCount(1));
		assertEquals(0, c.getHitCount(2));

		assertEquals(0, b.getHitCount(3));
		b.touch(3);
		assertEquals(1, b.getHitCount(3));
		for (int i = 0; i < 234; i++)
			b.touch(3);
		assertEquals(235, b.getHitCount(3));
	}

	public void testLineCoverageRate()
	{
		assertEquals(1d, a.getLineCoverageRate(), 0d);
		assertEquals(0.5d, b.getLineCoverageRate(), 0d);
		assertEquals(0d, c.getLineCoverageRate(), 0d);

		assertEquals(1d, a.getLineCoverageRate("test(I)B"), 0d);
		assertEquals(0.5d, b.getLineCoverageRate("test(I)B"), 0d);
		assertEquals(0d, c.getLineCoverageRate("test(I)B"), 0d);
	}

	public void testSourceFileName()
	{
		a.setSourceFileName(null);
		assertEquals(null, a.getSourceFileName());
		a.setSourceFileName("com/example/HelloWorld.java");
		assertEquals("com/example/HelloWorld.java", a.getSourceFileName());
	}

	public void testTouch()
	{
		int line = 3;

		assertFalse(a.isValidSourceLineNumber(line));
		a.touch(line);
		assertFalse(a.isValidSourceLineNumber(line));

		assertTrue(b.isValidSourceLineNumber(line));
		assertEquals(0, b.getHitCount(line));
		b.touch(line);
		assertTrue(b.isValidSourceLineNumber(line));
		assertEquals(1, b.getHitCount(line));
		b.touch(line);
		assertEquals(2, b.getHitCount(line));
		assertTrue(b.isValidSourceLineNumber(line));
	}

}
