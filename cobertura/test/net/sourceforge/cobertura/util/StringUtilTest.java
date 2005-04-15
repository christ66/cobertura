/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Jeremy Thomerson
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

import junit.framework.TestCase;

/**
 * @author Jeremy Thomerson
 */
public class StringUtilTest extends TestCase
{

	public void testReplace()
	{
		String result;
		result = StringUtil.replaceAll(
				"cobertura is a very, very cool coverage tool", "very",
				"really");
		assertEquals("cobertura is a really, really cool coverage tool",
				result);

		result = StringUtil.replaceAll("<init>V", "<", "&lt;");
		result = StringUtil.replaceAll(result, ">", "&gt;");
		assertEquals(result, "&lt;init&gt;V");

		result = StringUtil.replaceAll("<init>V", "<", "&lt;");
		result = StringUtil.replaceAll(result, ">", "&gt;");
		assertEquals(result, "&lt;init&gt;V");
	}

	public static void main(String[] args)
	{
		new StringUtilTest().testReplace();
	}
}