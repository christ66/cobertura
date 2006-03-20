/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

package net.sourceforge.cobertura.instrument;

import java.io.File;

class CoberturaFile extends File
{

	private static final long serialVersionUID = 0L;

	String baseDir;
	String pathname;

	CoberturaFile(String baseDir, String pathname)
	{
		super(baseDir, pathname);
		this.baseDir = baseDir;
		this.pathname = pathname;
	}

	/**
	 * @return True if file has an extension that matches one of the
	 *         standard java archives, false otherwise.
	 */
	boolean isArchive()
	{
		if (!isFile())
		{
			return false;
		}
		return pathname.endsWith(".jar") || pathname.endsWith(".zip") || pathname.endsWith(".war")
				|| pathname.endsWith(".ear") || pathname.endsWith(".sar");
	}

	/**
	 * @return True if file has "class" as its extension,
	 *         false otherwise.
	 */
	boolean isClass()
	{
		return isFile() && pathname.endsWith(".class");
	}

	public String toString()
	{
		return "pathname=" + pathname + " and baseDir=" + baseDir;
	}

}
