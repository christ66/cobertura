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

package net.sourceforge.cobertura.coverage;

import junit.framework.TestCase;

public class CoverageDataTest extends TestCase
{

	final CoverageData a = new CoverageData();
	final CoverageData b = new CoverageData();
	final CoverageData c = new CoverageData();

	public void setUp()
	{
		for (int i = 1; i < 5; i++)
			b.addLine(i, "test", "(I)B");
		for (int i = 1; i < 5; i++)
			c.addLine(i, "test", "(I)B");

		b.touch(1);
		b.touch(2);
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

	public void testGetLineCoverageRate()
	{
		assertEquals(1d, a.getLineCoverageRate(), 0d);
		assertEquals(0.5d, b.getLineCoverageRate(), 0d);
		assertEquals(0d, c.getLineCoverageRate(), 0d);
	}
}