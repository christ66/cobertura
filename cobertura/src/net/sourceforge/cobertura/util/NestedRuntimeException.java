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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

/**
 * J2SE 1.4 introduced a common API to record the fact that one
 * exception caused another, to access causative exceptions, and to
 * access the entire "causal chain" as part of the standard stack
 * backtrace.
 *
 *  <p>This class provides support for such "causal chains" under
 * earlier JDKs</p>
 * @see <a href="http://developer.java.sun.com/developer/bugParade/bugs/4209652.html">Chained Exception</a>
 */
public class NestedRuntimeException extends RuntimeException
{

	/**
	 * @serial detail include
	 */
	Throwable detail;
	static final String CONTAINED_EXCEPTION = "nested exception:";

	public NestedRuntimeException(String s)
	{
		super(s);
	}

	public NestedRuntimeException(String s, Throwable t)
	{
		super(s);
		try
		{
			getClass()
					.getMethod("initCause", new Class[] { Throwable.class })
					.invoke(this, new Object[] { t });
		}
		catch (NoSuchMethodException ex)
		{
			this.detail = t;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
		}
	}

	public NestedRuntimeException(Throwable t)
	{
		try
		{
			getClass()
					.getMethod("initCause", new Class[] { Throwable.class })
					.invoke(this, new Object[] { t });
		}
		catch (NoSuchMethodException ex)
		{
			this.detail = t;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
		}
	}

	boolean supportChainedException()
	{
		try
		{
			getClass()
					.getMethod("initCause", new Class[] { Throwable.class });
			return true;
		}
		catch (NoSuchMethodException ex)
		{
			return false;
		}
	}

	public void printStackTrace(PrintStream out)
	{
		super.printStackTrace(out);
		if ((!supportChainedException()) && (detail != null))
		{
			out.println(CONTAINED_EXCEPTION);
			detail.printStackTrace(out);
		}
	}

	public void printStackTrace(PrintWriter out)
	{
		super.printStackTrace(out);
		if ((!supportChainedException()) && (detail != null))
		{
			out.println(CONTAINED_EXCEPTION);
			detail.printStackTrace(out);
		}
	}

	public void printStackTrace()
	{
		printStackTrace(System.err);
	}

	public String toString()
	{
		if (supportChainedException())
		{
			return super.toString();
		}
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		if (detail != null)
		{
			sb.append(' ').append(CONTAINED_EXCEPTION).append(' ').append(
					detail);
		}
		return sb.toString();
	}
}