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

/**
 * A conditional records the position of a conditional branch
 * instruction, and the position of the branch target. Conditionals
 * are used internally by the instrumentation to determine branch
 * coverage.
 */
class ConditionalImpl implements Conditional, HasBeenInstrumented
{

	private static final long serialVersionUID = 1;

	int lineNumber;
	int targetLineNumber;

	ConditionalImpl(int lineNumber, int targetLineNumber)
	{
		this.lineNumber = lineNumber;
		this.targetLineNumber = targetLineNumber;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

	public int getTargetLineNumber()
	{
		return targetLineNumber;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		sb.append("line: ");
		sb.append(getLineNumber());
		sb.append(", target: ");
		sb.append(getTargetLineNumber());
		sb.append(']');
		return sb.toString();
	}
}