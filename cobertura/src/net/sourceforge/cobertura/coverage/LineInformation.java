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

package net.sourceforge.cobertura.coverage;

import java.io.Serializable;

public class LineInformation implements Serializable, HasBeenInstrumented
{
	private static final long serialVersionUID = 1;

	private long hits;
	private boolean isConditional;
	private final int lineNumber;
	private String methodName;

	LineInformation(int lineNumber)
	{
		this(lineNumber, null);
	}

	LineInformation(int lineNumber, String methodName)
	{
		this.hits = 0;
		this.isConditional = false;
		this.lineNumber = lineNumber;
		this.methodName = methodName;
	}

	long getHits()
	{
		return hits;
	}

	int getLineNumber()
	{
		return lineNumber;
	}

	String getMethodName()
	{
		return methodName;
	}

	boolean isConditional()
	{
		return isConditional;
	}

	public void setConditional(boolean isConditional)
	{
		this.isConditional = isConditional;
	}

	public void setMethodName(String methodName)
	{
		this.methodName = methodName;
	}

	void touch()
	{
		this.hits++;
	}

}