/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

package net.sourceforge.cobertura.reporting.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.cobertura.reporting.Util;
import net.sourceforge.cobertura.reporting.html.files.CopyFiles;

public class HTMLReport
{

	private File outputDir;

	private File sourceDir;

	private Coverage coverage;

	/**
	 * Create a coverage report
	 */
	public HTMLReport(Map coverageData, File outputDir, File sourceDir)
			throws Exception
	{
		this.outputDir = outputDir;
		this.sourceDir = sourceDir;
		this.coverage = CoverageUnmarshaller.parse(coverageData);

		removeNonexistantClasses();
		CopyFiles.copy(outputDir);
		generatePackageList();
		generateClassLists();
		generateOverviews();
		generateSourceFiles();
	}

	/**
	 * Go through the classes in our coverage data and remove any class
	 * for which we don't have source code.  These classes are usually
	 * RMI stubs or inner classes.
	 */
	private void removeNonexistantClasses()
	{
		Iterator iter = coverage.getClasses().iterator();
		while (iter.hasNext())
		{
			Clazz clazz = (Clazz)iter.next();
			File file = new File(sourceDir, clazz.getLongFileName());
			if (!file.isFile())
			{
				coverage.removeClass(clazz);
			}
		}
	}

	private void generatePackageList() throws IOException
	{
		File file = new File(outputDir, "frame-packages.html");
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
			Iterator iter = coverage.getPackages().iterator();
			while (iter.hasNext())
			{
				Package pkg = (Package)iter.next();
				String url1 = "frame-summary-" + pkg.getName() + ".html";
				String url2 = "frame-classes-" + pkg.getName() + ".html";
				out.println("<tr>");
				out.println("<td nowrap=\"nowrap\"><a href=\"" + url1
						+ "\" onClick='parent.classList.location.href=\""
						+ url2 + "\"' target=\"summary\">" + pkg.getName()
						+ "</a></td>");
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
		Iterator iter = coverage.getPackages().iterator();
		while (iter.hasNext())
		{
			Package pkg = (Package)iter.next();
			generateClassList(pkg);
		}
	}

	private void generateClassList(Package pkg) throws IOException
	{
		String filename;
		Set classes;
		if (pkg == null)
		{
			filename = "frame-classes.html";
			classes = coverage.getClasses();
		}
		else
		{
			filename = "frame-classes-" + pkg.getName() + ".html";
			classes = pkg.getClasses();
		}
		File file = new File(outputDir, filename);
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
			out.println(pkg == null ? "All Packages" : pkg.getName());
			out.println("</h5>");
			out.println("<h5>Classes</h5>");
			out.println("<table width=\"100%\">");

			for (Iterator iter = classes.iterator(); iter.hasNext();)
			{
				Clazz clazz = (Clazz)iter.next();
				out.println("<tr>");
				out
						.println("<td nowrap=\"nowrap\"><a target=\"summary\" href=\""
								+ clazz.getLongName()
								+ ".html\">"
								+ clazz.getName()
								+ "</a> <i>("
								+ getPercentValue(clazz.getLineCoverageRate())
								+ ")</i></td>");
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
		Iterator iter = coverage.getPackages().iterator();
		while (iter.hasNext())
		{
			Package pkg = (Package)iter.next();
			generateOverview(pkg);
		}
	}

	private void generateOverview(Package pkg) throws IOException
	{
		String filename;
		if (pkg == null)
		{
			filename = "frame-summary.html";
		}
		else
		{
			filename = "frame-summary-" + pkg.getName() + ".html";
		}
		File file = new File(outputDir, filename);
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
			out.print(pkg == null ? "All Packages" : pkg.getName());
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

			Set packages;
			if (pkg == null)
			{
				// Output a summary line for all packages
				out.println(generateTableRowForTotal());

				// Get packages
				packages = coverage.getPackages();

			}
			else
			{
				// Output a line for the current package
				out.println(generateTableRowForPackage(pkg));

				// Get subpackages
				packages = coverage.getSubPackages(pkg);
			}

			// Output a line for each package or subpackage
			if (packages.size() > 0)
			{
				Iterator iter = packages.iterator();
				while (iter.hasNext())
				{
					Package subpkg = (Package)iter.next();
					out.println(generateTableRowForPackage(subpkg));
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
			Set classes;
			if (pkg == null)
			{
				classes = new TreeSet();
				if (coverage.getClasses().size() > 0)
				{
					Iterator iter = coverage.getClasses().iterator();
					while (iter.hasNext())
					{
						Clazz clazz = (Clazz)iter.next();
						if (clazz.getPackageName() == null)
						{
							classes.add(clazz);
						}
					}
				}
			}
			else
			{
				classes = pkg.getClasses();
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
					Clazz clazz = (Clazz)iter.next();
					out.println(generateTableRowForClass(clazz));
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

	private void generateSourceFiles() throws IOException
	{
		Iterator iter = coverage.getClasses().iterator();
		while (iter.hasNext())
		{
			Clazz clazz = (Clazz)iter.next();
			generateSourceFile(clazz);
		}
	}

	private void generateSourceFile(Clazz clazz) throws IOException
	{
		String filename = clazz.getLongName() + ".html";
		File file = new File(outputDir, filename);
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
			if (clazz.getPackageName() != null)
			{
				out.print(clazz.getPackageName() + ".");
			}
			out.print(clazz.getName());
			out.println("</h5>");

			// Output the coverage summary for this class
			out.println("<p>");
			out.println("<table class=\"report\">");
			out.println(generateTableHeaderForClasses());
			out.println(generateTableRowForClass(clazz));
			out.println("</table>");
			out.println("</p>");

			// Output this class's source code with syntax and coverage highlighting
			out.println("<p>");
			out
					.println("<table cellspacing=\"0\" cellpadding=\"0\" class=\"src\">");
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new FileReader(new File(sourceDir,
						clazz.getLongFileName())));
				String lineStr;
				JavaToHtml javaToHtml = new JavaToHtml();
				int lineNumber = 1;
				while ((lineStr = br.readLine()) != null)
				{
					out.println("<tr>");
					if (clazz.isValidSourceLine(lineNumber))
					{
						long numberOfHits = clazz.getNumberOfHits(lineNumber);
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
		double lineCoverage = coverage.getLineCoverageRate();
		double branchCoverage = coverage.getBranchCoverageRate();
		double ccn = Util.getCCN(sourceDir, true);
		ret.append("  <tr>");
		ret.append("<td class=\"text\"><b>All Packages</b></td>");
		ret.append("<td class=\"value\">" + coverage.getNumberOfClasses()
				+ "</td>");
		ret.append(generateTableColumnsFromData(lineCoverage, branchCoverage,
				ccn));
		ret.append("</tr>");
		return ret.toString();
	}

	private String generateTableRowForPackage(Package pkg)
	{
		StringBuffer ret = new StringBuffer();
		String url1 = "frame-summary-" + pkg.getName() + ".html";
		String url2 = "frame-classes-" + pkg.getName() + ".html";
		double lineCoverage = pkg.getLineCoverageRate();
		double branchCoverage = pkg.getBranchCoverageRate();
		double ccn = Util.getCCN(new File(sourceDir, pkg.getFileName()),
				false);
		ret.append("  <tr>");
		ret.append("<td class=\"text\"><a href=\"" + url1
				+ "\" onClick='parent.classList.location.href=\"" + url2
				+ "\"'>" + pkg.getName() + "</a></td>");
		ret
				.append("<td class=\"value\">" + pkg.getClasses().size()
						+ "</td>");
		ret.append(generateTableColumnsFromData(lineCoverage, branchCoverage,
				ccn));
		ret.append("</tr>");
		return ret.toString();
	}

	private String generateTableRowForClass(Clazz clazz)
	{
		StringBuffer ret = new StringBuffer();
		double lineCoverage = clazz.getLineCoverageRate();
		double branchCoverage = clazz.getBranchCoverageRate();
		double ccn = Util.getCCN(
				new File(sourceDir, clazz.getLongFileName()), false);
		ret.append("  <tr>");
		ret.append("<td class=\"text\"><a href=\"" + clazz.getLongName()
				+ ".html\">" + clazz.getName() + "</a></td>");
		ret.append(generateTableColumnsFromData(lineCoverage, branchCoverage,
				ccn));
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
