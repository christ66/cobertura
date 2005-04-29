/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
 * Copyright (C) 2005 Grzegorz Lukasik <hauserx@users.sourceforge.net>
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

package net.sourceforge.cobertura.reporting.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.Util;
import net.sourceforge.cobertura.reporting.html.files.CopyFiles;

import org.apache.log4j.Logger;

public class HTMLReport
{

	private static final Logger logger = Logger.getLogger(HTMLReport.class);

	private File destinationDir;

	private File sourceDir;

	private ProjectData projectData;

	/**
	 * Create a coverage report
	 */
	public HTMLReport(ProjectData projectData, File outputDir, File sourceDir)
			throws Exception
	{
		this.destinationDir = outputDir;
		this.sourceDir = sourceDir;
		this.projectData = projectData;

		CopyFiles.copy(outputDir);
		generatePackageList();
		generateClassLists();
		generateOverviews();
		generateSourceFiles();
	}

	private String generatePackageName(PackageData packageData)
	{
		if (packageData.getName().equals(""))
			return "(default)";
		return packageData.getName();
	}

	private void generatePackageList() throws IOException
	{
		File file = new File(destinationDir, "frame-packages.html");
		PrintStream out = null;

		try
		{
			out = new PrintStream(new FileOutputStream(file));

			out.println("<html>");
			out.println("<head>");
			out.println("<title>Coverage Report</title>");
			out
					.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/main.css\" />");
			out.println("</head>");
			out.println("<body>");
			out.println("<h5>Packages</h5>");
			out.println("<table width=\"100%\">");
			out.println("<tr>");
			out
					.println("<td nowrap=\"nowrap\"><a href=\"frame-summary.html\" onClick='parent.classList.location.href=\"frame-classes.html\"' target=\"summary\">All</a></td>");
			out.println("</tr>");

			SortedSet sortedPackages = new TreeSet();
			sortedPackages.addAll(projectData.getChildren());
			Iterator iter = sortedPackages.iterator();
			while (iter.hasNext())
			{
				PackageData packageData = (PackageData)iter.next();
				String url1 = "frame-summary-" + packageData.getName()
						+ ".html";
				String url2 = "frame-classes-" + packageData.getName()
						+ ".html";
				out.println("<tr>");
				out.println("<td nowrap=\"nowrap\"><a href=\"" + url1
						+ "\" onClick='parent.classList.location.href=\""
						+ url2 + "\"' target=\"summary\">"
						+ generatePackageName(packageData) + "</a></td>");
				out.println("</tr>");
			}
			out.println("</table>");
			out.println("</body>");
			out.println("</html>");
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}
	}

	private void generateClassLists() throws IOException
	{
		generateClassList(null);
		Iterator iter = projectData.getChildren().iterator();
		while (iter.hasNext())
		{
			PackageData packageData = (PackageData)iter.next();
			generateClassList(packageData);
		}
	}

	private void generateClassList(PackageData packageData)
			throws IOException
	{
		String filename;
		Collection classes;
		if (packageData == null)
		{
			filename = "frame-classes.html";
			//TODO: Need to sort classes
			classes = projectData.getClasses();
		}
		else
		{
			filename = "frame-classes-" + packageData.getName() + ".html";
			classes = packageData.getChildren();
		}
		File file = new File(destinationDir, filename);
		PrintStream out = null;

		try
		{
			out = new PrintStream(new FileOutputStream(file));

			out.println("<html>");
			out.println("<head>");
			out.println("<title>Coverage Report Classes</title>");
			out
					.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/main.css\" />");
			out.println("</head>");
			out.println("<body>");
			out.println("<h5>");
			out.println(packageData == null
					? "All Packages"
					: generatePackageName(packageData));
			out.println("</h5>");
			out.println("<h5>Classes</h5>");
			out.println("<table width=\"100%\">");

			Iterator iter;
			Map sortedClassList = new TreeMap();
			iter = classes.iterator();
			while (iter.hasNext())
			{
				ClassData classData = (ClassData)iter.next();
				sortedClassList.put(classData.getBaseName(), classData);
			}
			iter = sortedClassList.values().iterator();
			while (iter.hasNext())
			{
				ClassData classData = (ClassData)iter.next();
				out.println("<tr>");
				String percentCovered;
				if (classData.getNumberOfValidLines() > 0)
					percentCovered = getPercentValue(classData
							.getLineCoverageRate());
				else
					percentCovered = "N/A";
				out
						.println("<td nowrap=\"nowrap\"><a target=\"summary\" href=\""
								+ classData.getName()
								+ ".html\">"
								+ classData.getBaseName()
								+ "</a> <i>("
								+ percentCovered + ")</i></td>");
				out.println("</tr>");
			}

			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("</body>");
			out.println("</html>");
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}
	}

	private void generateOverviews() throws IOException
	{
		generateOverview(null);
		Iterator iter = projectData.getChildren().iterator();
		while (iter.hasNext())
		{
			PackageData packageData = (PackageData)iter.next();
			generateOverview(packageData);
		}
	}

	private void generateOverview(PackageData packageData) throws IOException
	{
		String filename;
		if (packageData == null)
		{
			filename = "frame-summary.html";
		}
		else
		{
			filename = "frame-summary-" + packageData.getName() + ".html";
		}
		File file = new File(destinationDir, filename);
		PrintStream out = null;

		try
		{
			out = new PrintStream(new FileOutputStream(file));

			out.println("<html>");
			out.println("<head>");
			out.println("<title>Coverage Report</title>");
			out
					.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/main.css\" />");
			out
					.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/sortabletable.css\" />");
			out
					.println("<script type=\"text/javascript\" src=\"js/popup.js\"></script>");
			out
					.println("<script type=\"text/javascript\" src=\"js/sortabletable.js\"></script>");
			out
					.println("<script type=\"text/javascript\" src=\"js/percentagesorttype.js\"></script>");
			out.println("</head>");
			out.println("<body>");

			out.print("<h5>Coverage Report - ");
			out.print(packageData == null
					? "All Packages"
					: generatePackageName(packageData));
			out.println("</h5>");
			out.println("<p>");
			out.println("<table class=\"report\" id=\"packageResults\">");
			out.println("<thead>");
			out.println("<tr>");
			out.println("  <td class=\"heading\">Package</td>");
			out.println("  <td class=\"heading\"># Classes</td>");
			out.println(generateCommonTableColumns());
			out.println("</tr>");
			out.println("</thead>");
			out.println("<tbody>");

			Collection packages;
			if (packageData == null)
			{
				// Output a summary line for all packages
				out.println(generateTableRowForTotal());

				// Get packages
				packages = projectData.getChildren();
			}
			else
			{
				// Get subpackages
				packages = projectData.getSubPackages(packageData.getName());
			}

			// Output a line for each package or subpackage
			// TODO: Do we need this extra "package.size() > 0" check?
			if (packages.size() > 0)
			{
				Iterator iter = packages.iterator();
				while (iter.hasNext())
				{
					PackageData subPackageData = (PackageData)iter.next();
					out.println(generateTableRowForPackage(subPackageData));
				}
			}

			out.println("</tbody>");
			out.println("</table>");
			out.println("<script type=\"text/javascript\">");
			out
					.println("var packageTable = new SortableTable(document.getElementById(\"packageResults\"),");
			out
					.println("    [\"String\", \"Number\", \"Percentage\", \"Percentage\", \"Number\", \"Number\", \"Number\"]);");
			out.println("packageTable.sort(0);");
			out.println("</script>");
			out.println("</p>");

			// Get the list of classes in this package
			Collection classes;
			if (packageData == null)
			{
				classes = new TreeSet();
				if (projectData.getNumberOfClasses() > 0)
				{
					Iterator iter = projectData.getClasses().iterator();
					while (iter.hasNext())
					{
						ClassData classData = (ClassData)iter.next();
						if (classData.getPackageName() == null)
						{
							classes.add(classData);
						}
					}
				}
			}
			else
			{
				classes = packageData.getChildren();
			}

			// Output a line for each class
			if (classes.size() > 0)
			{
				out.println("<p>");
				out.println("<table class=\"report\" id=\"classResults\">");
				out.println(generateTableHeaderForClasses());
				out.println("<tbody>");

				Iterator iter = classes.iterator();
				while (iter.hasNext())
				{
					ClassData classData = (ClassData)iter.next();
					out.println(generateTableRowForClass(classData));
				}

				out.println("</tbody>");
				out.println("</table>");
				out.println("<script type=\"text/javascript\">");
				out
						.println("var classTable = new SortableTable(document.getElementById(\"classResults\"),");
				out
						.println("    [\"String\", \"Percentage\", \"Percentage\", \"Number\", \"Number\", \"Number\"]);");
				out.println("classTable.sort(0);");
				out.println("</script>");
				out.println("</p>");
			}

			out.println("<div class=\"footer\">");
			out
					.println("Report generated by <a href=\"http://cobertura.sourceforge.net/\" target=\"_top\">Cobertura</a>.");
			out.println("</div>");

			out.println("</body>");
			out.println("</html>");
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}
	}

	private void generateSourceFiles()
	{
		Iterator iter = projectData.getClasses().iterator();
		while (iter.hasNext())
		{
			ClassData classData = (ClassData)iter.next();
			try
			{
				generateSourceFile(classData);
			}
			catch (Exception e)
			{
				logger.info("Could not generate HTML file for class "
						+ classData.getName());
			}
		}
	}

	private void generateSourceFile(ClassData classData) throws IOException
	{
		String filename = classData.getName() + ".html";
		File file = new File(destinationDir, filename);
		PrintStream out = null;

		try
		{
			out = new PrintStream(new FileOutputStream(file));

			out.println("<html>");
			out.println("<head>");
			out.println("<title>Coverage Report</title>");
			out
					.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/main.css\" />");
			out
					.println("<script type=\"text/javascript\" src=\"js/popup.js\"></script>");
			out.println("</head>");
			out.println("<body>");
			out.print("<h5>Coverage Report - ");
			String classPackageName = classData.getPackageName();
			if ((classPackageName != null) && classPackageName.length() > 0)
			{
				out.print(classData.getPackageName() + ".");
			}
			out.print(classData.getName());
			out.println("</h5>");

			// Output the coverage summary for this class
			out.println("<p>");
			out.println("<table class=\"report\">");
			out.println(generateTableHeaderForClasses());
			out.println(generateTableRowForClass(classData));
			out.println("</table>");
			out.println("</p>");

			// Output this class's source code with syntax and coverage highlighting
			out.println("<p>");
			out
					.println("<table cellspacing=\"0\" cellpadding=\"0\" class=\"src\">");
			BufferedReader br = null;
			try
			{
				File sourceFile = new File(sourceDir, classData
						.getSourceFileName());
				br = new BufferedReader(new FileReader(sourceFile));
				String lineStr;
				JavaToHtml javaToHtml = new JavaToHtml();
				int lineNumber = 1;
				while ((lineStr = br.readLine()) != null)
				{
					out.println("<tr>");
					if (classData.isValidSourceLineNumber(lineNumber))
					{
						long numberOfHits = classData.getHitCount(lineNumber);
						out.println("  <td class=\"numLineCover\">&nbsp;"
								+ lineNumber + "</td>");
						if (numberOfHits > 0)
						{
							out
									.println("  <td class=\"nbHitsCovered\">&nbsp;"
											+ numberOfHits + "</td>");
							out
									.println("  <td class=\"src\"><pre class=\"src\">&nbsp;"
											+ javaToHtml.process(lineStr)
											+ "</pre></td>");
						}
						else
						{
							out
									.println("  <td class=\"nbHitsUncovered\">&nbsp;"
											+ numberOfHits + "</td>");
							out
									.println("  <td class=\"src\"><pre class=\"src\"><span class=\"srcUncovered\">&nbsp;"
											+ javaToHtml.process(lineStr)
											+ "</span></pre></td>");
						}
					}
					else
					{
						out.println("  <td class=\"numLine\">&nbsp;"
								+ lineNumber + "</td>");
						out.println("  <td class=\"nbHits\">&nbsp;</td>");
						out
								.println("  <td class=\"src\"><pre class=\"src\">&nbsp;"
										+ javaToHtml.process(lineStr)
										+ "</pre></td>");
					}
					out.println("</tr>");
					lineNumber++;
				}
			}
			finally
			{
				if (br != null)
				{
					br.close();
				}
			}
			out.println("</table>");
			out.println("</p>");

			out.println("<div class=\"footer\">");
			out
					.println("Reports generated by <a href=\"http://cobertura.sourceforge.net/\" target=\"_top\">Cobertura</a>.");
			out.println("</div>");

			out.println("</body>");
			out.println("</html>");
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}
	}

	private static String generateHelpURL(String text, String description)
	{
		StringBuffer ret = new StringBuffer();
		boolean popupTooltips = false;
		if (popupTooltips)
		{
			ret
					.append("<a class=\"hastooltip\" href=\"help.html\" onClick=\"popupwindow('help.html'); return false;\">");
			ret.append(text);
			ret.append("<span>" + description + "</span>");
			ret.append("</a>");
		}
		else
		{
			ret
					.append("<a class=\"dfn\" href=\"help.html\" onClick=\"popupwindow('help.html'); return false;\">");
			ret.append(text);
			ret.append("</a>");
		}
		return ret.toString();
	}

	private static String generateCommonTableColumns()
	{
		StringBuffer ret = new StringBuffer();
		ret.append("  <td class=\"heading\" width=\"20%\">"
				+ generateHelpURL("Line Coverage",
						"The percent of lines executed by this test run.")
				+ "</td>");
		ret.append("  <td class=\"heading\" width=\"20%\">"
				+ generateHelpURL("Branch Coverage",
						"The percent of branches executed by this test run.")
				+ "</td>");
		ret
				.append("  <td class=\"heading\" width=\"10%\">"
						+ generateHelpURL(
								"Complexity",
								"Average McCabe's cyclomatic code complexity for all methods.  This is basically a count of the number of different code paths in a method (incremented by 1 for each if statement, while loop, etc.)")
						+ "</td>");
		return ret.toString();
	}

	private static String generateTableHeaderForClasses()
	{
		StringBuffer ret = new StringBuffer();
		ret.append("<thead>");
		ret.append("<tr>");
		ret.append("  <td class=\"heading\">Classes in this Package</td>");
		ret.append(generateCommonTableColumns());
		ret.append("</tr>");
		ret.append("</thead>");
		return ret.toString();
	}

	private static String generateNAPercent()
	{
		StringBuffer sb = new StringBuffer();
		sb
				.append("<table cellpadding=\"0\" cellspacing=\"0\" align=\"right\">");
		sb.append("<tr>");
		sb
				.append("<td>"
						+ generateHelpURL(
								"N/A",
								"Line coverage and branch coverage will appear as \"Not Applicable\" when Cobertura can not find line number information in the .class file.  This happens for stub and skeleton classes, interfaces, or when the class was not compiled with \"debug=true.\"")
						+ "&nbsp;</td>");
		sb.append("<td>");
		sb
				.append("<table class=\"percentGraph\" cellpadding=\"0\" cellspacing=\"0\">");
		sb.append("<tr><td class=\"NA\" width=\"100\"></td></tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}

	private static String generateTableColumnsForNA(double ccn)
	{
		return "<td class=\"value\">" + generateNAPercent() + "</td>"
				+ "<td class=\"value\">" + generateNAPercent() + "</td>"
				+ "<td class=\"value\">" + getDoubleValue(ccn) + "</td>";

	}

	private static String generateTableColumnsFromData(double lineCoverage,
			double branchCoverage, double ccn)
	{
		return "<td class=\"value\">" + generatePercentResult(lineCoverage)
				+ "</td>" + "<td class=\"value\">"
				+ generatePercentResult(branchCoverage) + "</td>"
				+ "<td class=\"value\">" + getDoubleValue(ccn) + "</td>";

	}

	private String generateTableRowForTotal()
	{
		StringBuffer ret = new StringBuffer();
		double lineCoverage = projectData.getLineCoverageRate();
		double branchCoverage = projectData.getBranchCoverageRate();
		double ccn = Util.getCCN(sourceDir, true);
		ret.append("  <tr>");
		ret.append("<td class=\"text\"><b>All Packages</b></td>");
		ret.append("<td class=\"value\">" + projectData.getNumberOfClasses()
				+ "</td>");
		ret.append(generateTableColumnsFromData(lineCoverage, branchCoverage,
				ccn));
		ret.append("</tr>");
		return ret.toString();
	}

	private String generateTableRowForPackage(PackageData packageData)
	{
		StringBuffer ret = new StringBuffer();
		String url1 = "frame-summary-" + packageData.getName() + ".html";
		String url2 = "frame-classes-" + packageData.getName() + ".html";
		double lineCoverage = packageData.getLineCoverageRate();
		double branchCoverage = packageData.getBranchCoverageRate();
		double ccn = Util.getCCN(new File(sourceDir, packageData.getSourceFileName()), false);
		ret.append("  <tr>");
		ret.append("<td class=\"text\"><a href=\"" + url1
				+ "\" onClick='parent.classList.location.href=\"" + url2
				+ "\"'>" + generatePackageName(packageData) + "</a></td>");
		ret.append("<td class=\"value\">" + packageData.getChildren().size()
				+ "</td>");
		ret.append(generateTableColumnsFromData(lineCoverage, branchCoverage,
				ccn));
		ret.append("</tr>");
		return ret.toString();
	}

	private String generateTableRowForClass(ClassData classData)
	{
		StringBuffer ret = new StringBuffer();
		double lineCoverage = classData.getLineCoverageRate();
		double branchCoverage = classData.getBranchCoverageRate();
		double ccn = Util.getCCN(new File(sourceDir, classData
				.getSourceFileName()), false);
		ret.append("  <tr>");
		ret.append("<td class=\"text\"><a href=\"" + classData.getName()
				+ ".html\">" + classData.getName() + "</a></td>");
		if (classData.getNumberOfValidLines() == 0)
		{
			ret.append(generateTableColumnsForNA(ccn));
		}
		else
		{
			ret.append(generateTableColumnsFromData(lineCoverage,
					branchCoverage, ccn));
		}
		ret.append("</tr>");
		return ret.toString();
	}

	private static String generatePercentResult(double percentValue)
	{
		double rest = 1d - percentValue;
		StringBuffer sb = new StringBuffer();
		sb
				.append("<table cellpadding=\"0\" cellspacing=\"0\" align=\"right\">");
		sb.append("<tr>");
		sb.append("<td>" + getPercentValue(percentValue) + "&nbsp;</td>");
		sb.append("<td>");
		sb
				.append("<table class=\"percentGraph\" cellpadding=\"0\" cellspacing=\"0\">");
		sb.append("<tr>");
		sb.append("<td class=\"covered\" width=\""
				+ (int)(percentValue * 100) + "\"></td>");
		sb.append("<td class=\"uncovered\" width=\"" + (int)(rest * 100)
				+ "\"></td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}

	private static String getDoubleValue(double value)
	{
		NumberFormat formatter;
		formatter = new DecimalFormat();
		return formatter.format(value);
	}

	private static String getPercentValue(double value)
	{
		NumberFormat formatter;
		formatter = NumberFormat.getPercentInstance();
		return formatter.format(value);
	}

}
