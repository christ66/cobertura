/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
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

package net.sourceforge.cobertura.reporting.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.cobertura.coverage.CoverageData;

import org.apache.log4j.Logger;

public class XMLReport
{

	private static final Logger logger = Logger.getLogger(XMLReport.class);

	private final int indentRate = 2;
	private final PrintWriter pw;

	private int indent = 0;

	public XMLReport(Map coverageData, File outputDir, File sourceDirectory)
			throws IOException
	{
		pw = new PrintWriter(new FileWriter(new File(outputDir,
				"coverage.xml")));

		try
		{
			println("<?xml version=\"1.0\"?>");
			if (sourceDirectory == null)
			{
				println("<coverage>");
			}
			else
			{
				println("<coverage src=\"" + sourceDirectory + "\">");
			}
			increaseIndentation();
			dump(coverageData.entrySet().iterator());
			decreaseIndentation();
			println("</coverage>");
		}
		finally
		{
			pw.close();
		}
	}

	void increaseIndentation()
	{
		indent += indentRate;
	}

	void decreaseIndentation()
	{
		indent -= indentRate;
	}

	void indent()
	{
		for (int i = 0; i < indent; i++)
		{
			pw.print(' ');
		}
	}

	void println(String ln)
	{
		indent();
		pw.println(ln);
	}

	void dump(Iterator entySetIterator)
	{
		while (entySetIterator.hasNext())
		{
			dump((Map.Entry)entySetIterator.next());
		}
	}

	void dump(Map.Entry entry)
	{
		println("<class name=\"" + entry.getKey() + "\">");
		increaseIndentation();
		dump((String)entry.getKey(), (CoverageData)entry.getValue());
		decreaseIndentation();
		println("</class>");
	}

	private String getFileName(String className, CoverageData instrumentation)
	{
		// TODO: Find a better way to get this
		return "TODO";
		//return ClassHelper.getPackageName(className).replace('.', '/') + '/' + instrumentation.getSourceFileName();
	}

	private void dump(String className, CoverageData instrumentation)
	{
		println("<file name=\"" + getFileName(className, instrumentation)
				+ "\"/>");
		println("<line rate=\"" + instrumentation.getLineCoverageRate()
				+ "\"/>");
		println("<branch rate=\"" + instrumentation.getBranchCoverageRate()
				+ "\"/>");

		println("<methods>");
		increaseIndentation();
		dumpMethods(instrumentation);
		decreaseIndentation();
		println("</methods>");

		StringBuffer sb = new StringBuffer();
		Iterator iter = instrumentation.getValidLineNumbers().iterator();
		while (iter.hasNext())
		{
			sb.append(iter.next());
			if (iter.hasNext())
			{
				sb.append(", ");
			}
		}
		println("<valid lines=\"" + sb.toString() + "\"/>");

		iter = instrumentation.getValidLineNumbers().iterator();
		while (iter.hasNext())
		{
			int lineNumber = ((Integer)iter.next()).intValue();
			long hitCount = instrumentation.getHitCount(lineNumber);
			println("<line number=\"" + lineNumber + "\" hits=\"" + hitCount
					+ "\"/>");
		}
	}

	private String xmlEscape(String str)
	{
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(">", "&gt;");
		return str;
	}

	private void dumpMethods(CoverageData instrumentation)
	{
		Iterator iter = instrumentation.getMethodNamesAndDescriptors()
				.iterator();
		while (iter.hasNext())
		{

			String methodNameAndSignature = (String)iter.next();

			println("<method nameAndSignature=\""
					+ xmlEscape(methodNameAndSignature)	+ "\">");
			increaseIndentation();

			try
			{
				println("<line rate=\""
						+ instrumentation
								.getLineCoverageRate(methodNameAndSignature)
						+ "\"/>");
				println("<branch rate=\""
						+ instrumentation
								.getBranchCoverageRate(methodNameAndSignature)
						+ "\"/>");
			}
			catch (IllegalArgumentException ex)
			{
				logger.warn(ex);
			}

			decreaseIndentation();
			println("</method>");
		}
	}

}
