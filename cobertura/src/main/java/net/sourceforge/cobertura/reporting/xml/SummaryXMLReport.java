/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2008 Julian Gamble 
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

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Header;
import net.sourceforge.cobertura.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class SummaryXMLReport extends CommonXMLReport {
	public SummaryXMLReport(ProjectData projectData, File destinationDir,
			FileFinder finder, ComplexityCalculator complexity)
			throws IOException {
		File file = new File(destinationDir, "coverage-summary.xml");
		setPrintWriter(IOUtil.getPrintWriter(file));

		try {
			printHeader();
			printCoverageElement(projectData, complexity);

			//the DTD requires a "packages" element
			increaseIndentation();
			println("<packages />");
			decreaseIndentation();

			println("</coverage>");
		} finally {
			close();
		}
	}
}
