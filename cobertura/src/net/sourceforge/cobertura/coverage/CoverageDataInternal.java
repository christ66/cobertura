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

import java.util.Map;
import java.util.Set;

/**
 * This is an internal interface used only by instrumentation. When a
 * class is first instrumented by instrumentation certain information
 * is serialized to disk (e.g., the valid source line numbers, the
 * source file name, the line numbers by method, the line number of
 * each conditional branch and its target for each method, and the
 * method name and signature of each method found in the instrumented
 * class.
 */
interface CoverageDataInternal extends CoverageData
{

	void setConditionals(Set conditionals);

	void setConditionalsByMethod(Map conditionalsByMethod);

	void setMethodNamesAndSignatures(Set methodNamesAndSignatures);

	/**
	 * @param sourceFileName the source file name.
	 */
	void setSourceFileName(String sourceFileName);

	/**
	 * @param sourceLineNumbers the set of valid source line numbers.
	 */
	void setSourceLineNumbers(Set sourceLineNumbers);

	void setSourceLineNumbersByMethod(Map sourceLineNumbersByMethod);
}

