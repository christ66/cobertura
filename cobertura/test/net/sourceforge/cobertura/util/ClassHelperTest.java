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

package net.sourceforge.cobertura.util;

import junit.framework.TestCase;

public class ClassHelperTest extends TestCase
{

	public void testGetPackageName()
	{
		assertEquals("java.lang", ClassHelper.getPackageName(String.class));
		assertEquals("", ClassHelper.getPackageName("HelloWorld"));
	}

	public void testGetBaseName()
	{
		assertEquals("String", ClassHelper.getBaseName(String.class));
		assertEquals("HelloWorld", ClassHelper.getBaseName("HelloWorld"));
	}

	void checkPrimitiveWrapper(Class primitive, Class wrapper)
	{
		assertTrue(ClassHelper.getPrimitiveWrapper(primitive)
				.isAssignableFrom(wrapper));
	}

	public void testGetPrimitiveWrapper()
	{
		checkPrimitiveWrapper(boolean.class, Boolean.class);
		checkPrimitiveWrapper(char.class, Character.class);
		checkPrimitiveWrapper(byte.class, Byte.class);
		checkPrimitiveWrapper(short.class, Short.class);
		checkPrimitiveWrapper(int.class, Integer.class);
		checkPrimitiveWrapper(long.class, Long.class);
		checkPrimitiveWrapper(float.class, Float.class);
		checkPrimitiveWrapper(double.class, Double.class);
		try
		{
			ClassHelper.getPrimitiveWrapper(getClass());
			fail("Expected an Illegal ArgumentException but did not receive one!");
		}
		catch (IllegalArgumentException e)
		{
			//Good
		}
	}
}