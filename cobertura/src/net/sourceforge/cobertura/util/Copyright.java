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

import java.io.PrintStream;

public abstract class Copyright
{
	public static final int NAME = 0;
	public static final int YEARS = 1;

	public static final String[][] COPYRIGHT = new String[][] {
			{ "jcoverage ltd.", "2003" },
			{ "Mark Doliner <thekingant@users.sourceforge.net>", "2005" } };

	public static void print(PrintStream out)
	{
		out.println("Cobertura " + Version.VERSION_STRING);
		for (int i = 0; i < COPYRIGHT.length; i++)
		{
			out.println("Copyright (C) " + COPYRIGHT[i][YEARS] + " "
					+ COPYRIGHT[i][NAME]);
		}
		out
				.println("Cobertura is licensed under the GNU General Public License");
		out.println("Cobertura comes with ABSOLUTELY NO WARRANTY");
	}
}