/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2006 John Lewis
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License (so that it can be used from both the main
 * Cobertura classes and the ant tasks).
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

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Abstract, not to be instantiated utility class for Regex functions.
 *
 * @author John Lewis (logic copied from MethodInstrumenter)
 */
public abstract class RegexUtil {

	private static final Logger logger = Logger.getLogger(RegexUtil.class);

	/**
	 * <p>
	 * Check to see if one of the regular expressions in a collection match
	 * an input string.
	 * </p>
	 *
	 * @param regexs The collection of regular expressions.
	 * @param str    The string to check for a match.
	 *
	 * @return True if a match is found.
	 */
	public static boolean matches(Collection regexs, String str) {
		Iterator iter = regexs.iterator();
		while (iter.hasNext()) {
			Pattern regex = (Pattern) iter.next();
			Matcher m = regex.matcher(str);
			if (m.matches()) {
				return true;
			}
		}

		return false;
	}

	public static void addRegex(Collection list, String regex) {
		try {
			Pattern pattern = Pattern.compile(regex);
			list.add(pattern);
		} catch (PatternSyntaxException pse) {
			logger.warn("The regular expression " + regex + " is invalid: "
					+ pse.getLocalizedMessage());
		}
	}

}
