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

package net.sourceforge.cobertura.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.bcel.classfile.JavaClass;
import org.apache.log4j.Logger;

public abstract class JavaClassHelper
{

	private static Logger logger = Logger.getLogger(JavaClassHelper.class);

	public static JavaClass newJavaClass(InputStream clazz, String name)
			throws IOException
	{
		return ClassParserHelper.newClassParser(clazz, name).parse();
	}

	public static JavaClass newJavaClass(Class cl) throws IOException
	{
		return ClassParserHelper.newClassParser(cl).parse();
	}

	public static void dump(JavaClass javaClass) throws IOException
	{
		PrintWriter pw = null;

		try
		{
			String fileName = ClassHelper.getBaseName(javaClass
					.getClassName())
					+ ".code";

			logger.debug("dumping: " + javaClass.getClassName() + " to: "
					+ fileName);

			pw = new PrintWriter(new FileWriter(fileName));

			pw.println(javaClass);
			pw.println(javaClass.getConstantPool());
			for (int i = 0; i < javaClass.getMethods().length; i++)
			{
				pw.println(javaClass.getMethods()[i]);
				if (javaClass.getMethods()[i].getCode() != null)
				{
					pw.println(javaClass.getMethods()[i].getCode().toString(
							true));
				}
			}

			pw.close();
		}
		finally
		{
			if (pw != null)
			{
				pw.close();
			}
		}
	}
}