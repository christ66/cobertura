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

public abstract class ClassHelper
{

	public static String getBaseName(Class cl)
	{
		return getBaseName(cl.getName());
	}

	public static String getBaseName(String cl)
	{
		int lastDot = cl.lastIndexOf('.');
		if (lastDot == -1)
		{
			return cl;
		}
		return cl.substring(lastDot + 1);
	}

	public static String getPackageName(Class cl)
	{
		return getPackageName(cl.getName());
	}

	public static String getPackageName(String cl)
	{
		int lastDot = cl.lastIndexOf('.');
		if (lastDot == -1)
		{
			return "";
		}
		return cl.substring(0, lastDot);
	}

	public static Class getPrimitiveWrapper(Class cl)
	{
		if (cl.isPrimitive())
		{
			if (cl.equals(boolean.class))
			{
				return Boolean.class;
			}
			else if (cl.equals(char.class))
			{
				return Character.class;
			}
			else if (cl.equals(byte.class))
			{
				return Byte.class;
			}
			else if (cl.equals(short.class))
			{
				return Short.class;
			}
			else if (cl.equals(int.class))
			{
				return Integer.class;
			}
			else if (cl.equals(long.class))
			{
				return Long.class;
			}
			else if (cl.equals(float.class))
			{
				return Float.class;
			}
			else if (cl.equals(double.class))
			{
				return Double.class;
			}
		}

		throw new IllegalArgumentException(cl.getName());
	}
}