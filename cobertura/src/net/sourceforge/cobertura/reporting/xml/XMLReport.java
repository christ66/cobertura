/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
 * Copyright (C) 2005 Jeremy Thomerson <jthomerson@users.sourceforge.net>
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

import net.sourceforge.cobertura.coverage.CoverageData;
import net.sourceforge.cobertura.reporting.Clazz;
import net.sourceforge.cobertura.reporting.CoverageReport;
import net.sourceforge.cobertura.reporting.Package;

import org.apache.log4j.Logger;

public class XMLReport
{

	private static final Logger logger = Logger.getLogger(XMLReport.class);

	private final int indentRate = 2;
	private final PrintWriter pw;

	private int indent = 0;

	public XMLReport(CoverageReport coverage, File outputDir,
			File sourceDirectory) throws IOException
	{
		pw = new PrintWriter(new FileWriter(new File(outputDir,
				"coverage.xml")));

		try
		{
			println("<?xml version=\"1.0\"?>");
			println("<!DOCTYPE coverage SYSTEM \"http://cobertura.sourceforge.net/xml/coverage.dtd\">");
			println("");

			if (sourceDirectory == null)
			{
				// TODO: Set a schema?
				//println("<coverage xmlns=\"http://cobertura.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://cobertura.sourceforge.net/xml/coverage.xsd\">");
				println("<coverage>");
			}
			else
			{
				// TODO: Set a schema?
				//println("<coverage src=\"" + sourceDirectory + "\" xmlns=\"http://cobertura.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://cobertura.sourceforge.net/xml/coverage.xsd\">");
				println("<coverage src=\"" + sourceDirectory + "\">");
			}
			increaseIndentation();
			dumpPackages(coverage);
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
			pw.print("  ");
		}
	}

	void println(String ln)
	{
		indent();
		pw.println(ln);
	}

	private void dumpPackages(CoverageReport coverage)
	{
		println("<packages>");
		increaseIndentation();

		Iterator it = coverage.getPackages().iterator();
		while (it.hasNext())
		{
			dumpPackage((Package)it.next());
		}

		decreaseIndentation();
		println("</packages>");
	}

	private void dumpPackage(Package pack)
	{
		logger.debug("Dumping package " + pack.getName());

		println("<package name=\"" + pack.getName() + "\" line-rate=\""
				+ pack.getLineCoverageRate() + "\" branch-rate=\""
				+ pack.getBranchCoverageRate() + "\"" + ">");
		increaseIndentation();
		dumpClasses((Clazz[])pack.getClasses().toArray(
				new Clazz[pack.getClasses().size()]));
		decreaseIndentation();
		println("</package>");
	}

	private void dumpClasses(Clazz[] clazzes)
	{
		println("<classes>");
		increaseIndentation();

		for (int i = 0; i < clazzes.length; i++)
		{
			dumpClass(clazzes[i]);
		}

		decreaseIndentation();
		println("</classes>");
	}

	private void dumpClass(Clazz clazz)
	{
		logger.debug("Dumping class " + clazz.getLongName());

		println("<class name=\"" + clazz.getLongName() + "\" filename=\""
				+ clazz.getLongFileName() + "\" line-rate=\""
				+ clazz.getLineCoverageRate() + "\" branch-rate=\""
				+ clazz.getBranchCoverageRate() + "\"" + ">");
		increaseIndentation();

		dumpMethods(clazz);
		dumpLines(clazz);

		decreaseIndentation();
		println("</class>");
	}

	private void dumpMethods(Clazz clazz)
	{
		println("<methods>");
		increaseIndentation();

		CoverageData coverageData = clazz.getRawCoverageData();
		Iterator iter = coverageData.getMethodNamesAndDescriptors()
				.iterator();
		while (iter.hasNext())
		{
			dumpMethod(coverageData, (String)iter.next());
		}

		decreaseIndentation();
		println("</methods>");
	}

	private void dumpMethod(CoverageData coverageData, String nameAndSig)
	{
		String name = nameAndSig.substring(0, nameAndSig.indexOf('('));
		String signature = nameAndSig.substring(nameAndSig.indexOf('('));
		double lineRate = coverageData.getLineCoverageRate(nameAndSig);
		double branchRate = coverageData.getBranchCoverageRate(nameAndSig);

		println("<method name=\"" + xmlEscape(name) + "\" signature=\""
				+ xmlEscape(signature) + "\" line-rate=\"" + lineRate
				+ "\" branch-rate=\"" + branchRate + "\"/>");
	}

	private static String xmlEscape(String str)
	{
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(">", "&gt;");
		return str;
	}

	private void dumpLines(Clazz clazz)
	{
		println("<lines>");
		increaseIndentation();

		CoverageData coverageData = clazz.getRawCoverageData();
		Iterator iter = coverageData.getValidLineNumbers().iterator();
		iter = coverageData.getValidLineNumbers().iterator();
		while (iter.hasNext())
		{
			dumpLine(coverageData, (Integer)iter.next());
		}

		decreaseIndentation();
		println("</lines>");
	}

	private void dumpLine(CoverageData coverageData, Integer lineNumberObject)
	{
		int lineNumber = lineNumberObject.intValue();
		long hitCount = coverageData.getHitCount(lineNumber);
		boolean isBranch = coverageData.isBranch(lineNumber);

		println("<line number=\"" + lineNumber + "\" hits=\"" + hitCount
				+ "\" branch=\"" + isBranch + "\"/>");
	}

}