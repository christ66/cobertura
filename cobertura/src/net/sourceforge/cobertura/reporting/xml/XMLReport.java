/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
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
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.LineData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.reporting.Util;
import net.sourceforge.cobertura.util.StringUtil;

import org.apache.log4j.Logger;

public class XMLReport
{

	private static final Logger logger = Logger.getLogger(XMLReport.class);

	protected final static String coverageDTD = "coverage-02.dtd";

	private final PrintWriter pw;

	private int indent = 0;

	private File sourceDirectory;

	public XMLReport(ProjectData projectData, File destinationDir,
			File sourceDirectory) throws IOException
	{
		this.sourceDirectory = sourceDirectory;

		pw = new PrintWriter(new FileWriter(new File(destinationDir,
				"coverage.xml")));

		try
		{
			println("<?xml version=\"1.0\"?>");
			println("<!DOCTYPE coverage SYSTEM \"http://cobertura.sourceforge.net/xml/"
					+ coverageDTD + "\">");
			println("");

			StringBuffer sourceDirectories = new StringBuffer();
			if (sourceDirectory != null)
				sourceDirectories.append("src=\"" + sourceDirectory + "\"");

			// TODO: Set a schema?
			//println("<coverage " + sourceDirectories.toString() + " xmlns=\"http://cobertura.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://cobertura.sourceforge.net/xml/coverage.xsd\">");
			println("<coverage line-rate=\""
					+ projectData.getLineCoverageRate() + "\" branch-rate=\""
					+ projectData.getBranchCoverageRate() + "\" "
					+ sourceDirectories.toString() + ">");

			increaseIndentation();
			dumpPackages(projectData);
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
		indent++;
	}

	void decreaseIndentation()
	{
		if (indent > 0)
			indent--;
	}

	void indent()
	{
		for (int i = 0; i < indent; i++)
		{
			pw.print("\t");
		}
	}

	void println(String ln)
	{
		indent();
		pw.println(ln);
	}

	private void dumpPackages(ProjectData projectData)
	{
		println("<packages>");
		increaseIndentation();

		Iterator it = projectData.getPackages().iterator();
		while (it.hasNext())
		{
			dumpPackage((PackageData)it.next());
		}

		decreaseIndentation();
		println("</packages>");
	}

	private void dumpPackage(PackageData packageData)
	{
		logger.debug("Dumping package " + packageData.getName());

		double ccn = Util.getCCN(new File(sourceDirectory, packageData
				.getSourceFileName()), false);
		println("<package name=\"" + packageData.getName()
				+ "\" line-rate=\"" + packageData.getLineCoverageRate()
				+ "\" branch-rate=\"" + packageData.getBranchCoverageRate()
				+ "\" complexity=\"" + ccn + "\"" + ">");
		increaseIndentation();
		dumpSourceFiles(packageData);
		decreaseIndentation();
		println("</package>");
	}

	private void dumpSourceFiles(PackageData packageData)
	{
		println("<classes>");
		increaseIndentation();

		Iterator it = packageData.getSourceFiles().iterator();
		while (it.hasNext())
		{
			dumpClasses((SourceFileData)it.next());
		}

		decreaseIndentation();
		println("</classes>");
	}

	private void dumpClasses(SourceFileData sourceFileData)
	{
		Iterator it = sourceFileData.getClasses().iterator();
		while (it.hasNext())
		{
			dumpClass((ClassData)it.next());
		}
	}

	private void dumpClass(ClassData classData)
	{
		logger.debug("Dumping class " + classData.getName());

		double ccn = Util.getCCN(new File(sourceDirectory, classData
				.getSourceFileName()), false);
		println("<class name=\"" + classData.getName() + "\" filename=\""
				+ classData.getSourceFileName() + "\" line-rate=\""
				+ classData.getLineCoverageRate() + "\" branch-rate=\""
				+ classData.getBranchCoverageRate() + "\" complexity=\""
				+ ccn + "\"" + ">");
		increaseIndentation();

		dumpMethods(classData);
		dumpLines(classData);

		decreaseIndentation();
		println("</class>");
	}

	private void dumpMethods(ClassData classData)
	{
		println("<methods>");
		increaseIndentation();

		SortedSet sortedMethods = new TreeSet();
		sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
		Iterator iter = sortedMethods.iterator();
		while (iter.hasNext())
		{
			dumpMethod(classData, (String)iter.next());
		}

		decreaseIndentation();
		println("</methods>");
	}

	private void dumpMethod(ClassData classData, String nameAndSig)
	{
		String name = nameAndSig.substring(0, nameAndSig.indexOf('('));
		String signature = nameAndSig.substring(nameAndSig.indexOf('('));
		double lineRate = classData.getLineCoverageRate(nameAndSig);
		double branchRate = classData.getBranchCoverageRate(nameAndSig);

		println("<method name=\"" + xmlEscape(name) + "\" signature=\""
				+ xmlEscape(signature) + "\" line-rate=\"" + lineRate
				+ "\" branch-rate=\"" + branchRate + "\">");
		increaseIndentation();
		dumpLines(classData, nameAndSig);
		decreaseIndentation();
		println("</method>");
	}

	private static String xmlEscape(String str)
	{
		str = StringUtil.replaceAll(str, "<", "&lt;");
		str = StringUtil.replaceAll(str, ">", "&gt;");
		return str;
	}

	private void dumpLines(ClassData classData)
	{
		dumpLines(classData.getLines());
	}

	private void dumpLines(ClassData classData, String methodNameAndSig)
	{
		dumpLines(classData.getLines(methodNameAndSig));
	}

	private void dumpLines(Collection lines)
	{
		println("<lines>");
		increaseIndentation();

		SortedSet sortedLines = new TreeSet();
		sortedLines.addAll(lines);
		Iterator iter = sortedLines.iterator();
		while (iter.hasNext())
		{
			dumpLine((LineData)iter.next());
		}

		decreaseIndentation();
		println("</lines>");
	}

	private void dumpLine(LineData lineData)
	{
		int lineNumber = lineData.getLineNumber();
		long hitCount = lineData.getHits();
		boolean isBranch = lineData.isBranch();

		println("<line number=\"" + lineNumber + "\" hits=\"" + hitCount
				+ "\" branch=\"" + isBranch + "\"/>");
	}

}
