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

package net.sourceforge.cobertura.util;

import junit.framework.TestCase;

import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

public class TypeHelperTest extends TestCase
{

	public void testGetTypeOnPrimitive()
	{
		assertEquals(Type.BOOLEAN, TypeHelper.getType(boolean.class));
		assertEquals(Type.CHAR, TypeHelper.getType(char.class));
		assertEquals(Type.BYTE, TypeHelper.getType(byte.class));
		assertEquals(Type.SHORT, TypeHelper.getType(short.class));
		assertEquals(Type.INT, TypeHelper.getType(int.class));
		assertEquals(Type.LONG, TypeHelper.getType(long.class));
		assertEquals(Type.FLOAT, TypeHelper.getType(float.class));
		assertEquals(Type.DOUBLE, TypeHelper.getType(double.class));
	}

	public void testGetTypeOnArray()
	{
		ArrayType arrayType;

		arrayType = (ArrayType)TypeHelper.getType(char[].class);
		assertEquals(Type.CHAR, arrayType.getBasicType());
		assertEquals(1, arrayType.getDimensions());
	}

	public void testGetTypeOnVoid()
	{
		assertEquals(Type.VOID, TypeHelper.getType(void.class));
	}

	public void testGetTypeOnObject()
	{
		ObjectType objectType;

		objectType = (ObjectType)TypeHelper.getType(String.class);
		assertEquals(String.class.getName(), objectType.getClassName());
	}

	public void testGetTypes()
	{
		assertEquals(0, TypeHelper.getTypes(new Class[] { }).length);

		Type[] types = TypeHelper.getTypes(new Class[] { int.class,
				float.class, double.class });
		assertEquals(3, types.length);
		assertEquals(Type.INT, types[0]);
		assertEquals(Type.FLOAT, types[1]);
		assertEquals(Type.DOUBLE, types[2]);
	}
}