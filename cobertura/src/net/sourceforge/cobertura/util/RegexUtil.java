/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2006 John Lewis
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

import java.util.Collection;
import java.util.Iterator;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Abstract, not to be instantiated utility class for Regex functions.
 * 
 * @author John Lewis (logic copied from MethodInstrumenter)
 */
public abstract class RegexUtil
{

	private final static Perl5Matcher pm = new Perl5Matcher();

	/**
	 * <p>
	 * Check to see if one of the regular expressions in a collection match
	 * an input string.
	 * </p>
	 *
	 * @param regexs The collection of regular expressions.
	 * @param str The string to check for a match.
	 * @return True if a match is found.
	 */
	public static boolean matches(Collection regexs, String str)
	{
		Iterator iter = regexs.iterator();
		while (iter.hasNext())
		{
			Pattern regex = (Pattern)iter.next();
			if (pm.matches(str, regex))
			{
				return true;
			}
		}

		return false;
	}

}
