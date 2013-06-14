/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Jeremy Thomerson
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

import java.text.NumberFormat;

/**
 * Abstract, not to be instantiated utility class for String functions.
 *
 * @author Jeremy Thomerson
 */
public abstract class StringUtil {

	/**
	 * <p>
	 * Replaces all instances of "replace" with "with" from the "original"
	 * string.
	 * </p>
	 * <p/>
	 * <p>
	 * NOTE: it is known that a similar function is included in jdk 1.4 as replaceAll(),
	 * but is written here so as to allow backward compatibility to users using SDK's
	 * prior to 1.4
	 * </p>
	 *
	 * @param original The original string to do replacement on.
	 * @param replace  The string to replace.
	 * @param with     The string to replace "replace" with.
	 *
	 * @return The replaced string.
	 */
	public static String replaceAll(String original, String replace, String with) {
		if (original == null) {
			return original;
		}

		final int len = replace.length();
		StringBuffer sb = new StringBuffer(original.length());
		int start = 0;
		int found = -1;

		while ((found = original.indexOf(replace, start)) != -1) {
			sb.append(original.substring(start, found));
			sb.append(with);
			start = found + len;
		}

		sb.append(original.substring(start));
		return sb.toString();
	}

	/**
	 * Takes a double and turns it into a percent string.
	 * Ex.  0.5 turns into 50%
	 *
	 * @param value
	 *
	 * @return corresponding percent string
	 */
	public static String getPercentValue(double value) {
		//moved from HTMLReport.getPercentValue()
		value = Math.floor(value * 100) / 100; //to represent 199 covered lines from 200 as 99% covered, not 100 %
		return NumberFormat.getPercentInstance().format(value);
	}

}
