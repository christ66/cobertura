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

package net.sourceforge.cobertura.reporting;

import junit.framework.TestCase;

public class ClazzTest extends TestCase
{

	public static void testClazz()
	{
		Clazz clazz;

		clazz = new Clazz("HelloWorld");
		assertEquals("HelloWorld", clazz.getName());
		assertEquals("", clazz.getPackageName());
		assertEquals("HelloWorld.java", clazz.getLongFileName());
		assertEquals("HelloWorld", clazz.getLongName());
		assertEquals(0, clazz.getBranchCoverageRate(), 0);
		assertEquals(1.0, clazz.getLineCoverageRate(), 0);
		assertFalse(clazz.isValidSourceLine(19));

		clazz = new Clazz("com.example.HelloWorld");
		assertEquals("HelloWorld", clazz.getName());
		assertEquals("com.example", clazz.getPackageName());
		assertEquals("com/example/HelloWorld.java", clazz.getLongFileName());
		assertEquals("com.example.HelloWorld", clazz.getLongName());

		try
		{
			new Clazz(null);
			fail("Expected an IllegalArgumentException but did not receive one!");
		}
		catch (IllegalArgumentException e)
		{
			//Good
		}
	}

}
