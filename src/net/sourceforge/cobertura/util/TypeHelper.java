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

import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

public abstract class TypeHelper
{

	public static Type getType(Class cl)
	{
		if (cl.equals(boolean.class))
		{
			return Type.BOOLEAN;
		}
		else if (cl.equals(char.class))
		{
			return Type.CHAR;
		}
		else if (cl.equals(byte.class))
		{
			return Type.BYTE;
		}
		else if (cl.equals(short.class))
		{
			return Type.SHORT;
		}
		else if (cl.equals(int.class))
		{
			return Type.INT;
		}
		else if (cl.equals(long.class))
		{
			return Type.LONG;
		}
		else if (cl.equals(float.class))
		{
			return Type.FLOAT;
		}
		else if (cl.equals(double.class))
		{
			return Type.DOUBLE;
		}
		else if (cl.isArray())
		{
			return new ArrayType(getType(cl.getComponentType()), 1);
		}
		else if (cl.equals(void.class))
		{
			return Type.VOID;
		}
		else
		{
			return new ObjectType(cl.getName());
		}
	}

	public static Type[] getTypes(Class[] cls)
	{
		Type[] types = new Type[cls.length];
		for (int i = 0; i < cls.length; i++)
		{
			types[i] = getType(cls[i]);
		}
		return types;
	}
}