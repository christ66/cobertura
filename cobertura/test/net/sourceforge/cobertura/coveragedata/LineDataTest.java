/*
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
		assertTrue(a.equals(a));
		assertFalse(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(a.equals(d));
		assertFalse(a.equals(e));
		assertFalse(a.equals(f));

		LineData aPrime = new LineData(10, "test1", "(I)B");
		assertTrue(a.equals(aPrime));
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

	public void testSetConditional()
	{
		assertFalse(c.isBranch());
		c.setBranch(true);
		assertTrue(c.isBranch());
		c.setBranch(false);
		assertFalse(c.isBranch());
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
