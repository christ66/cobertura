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

/**
 * A conditional records the position of a conditional branch
 * instruction, and the position of the branch target. Conditionals
 * are used internally by the instrumentation to determine branch
 * coverage.
 */
interface Conditional extends Serializable
{

	/**
	 * The line number of this conditional branch. (Actually, it's the
	 * source line immediately after the conditional branch, so that
	 * instrumentation can determine whether the "fall-through" branch
	 * has been executed).
	 */
	int getLineNumber();

	/**
	 * The target line number of this conditional branch.
	 */
	int getTargetLineNumber();
}