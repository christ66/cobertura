package net.sourceforge.cobertura.reporting.xml;

import java.io.PrintWriter;
import java.util.Date;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.util.Header;

public class CommonXMLReport {

	private int indent = 0;
	private PrintWriter pw;

	public void setPrintWriter(PrintWriter pw) {
		this.pw = pw;
	}

	/**
	 * Prints the following header in the xml file:
	 * 
	 * <?xml version=\"1.0\"?>
	 * <!DOCTYPE coverage SYSTEM \"http://cobertura.sourceforge.net/xml/coverage-04.dtd" "\">
	 *
	 */
	public void printHeader() {
		println("<?xml version=\"1.0\"?>");
		println("<!DOCTYPE coverage SYSTEM \"http://cobertura.sourceforge.net/xml/"
				+ XMLReport.coverageDTD + "\">");
		println("");

	}

	/**
	 * Prints <coverage> element to the printwriter given information from the ProjectData and
	 * ComplexityCalculator arguments.
	 * 
	 * @param projectData
	 * @param complexity
	 */
	public void printCoverageElement(ProjectData projectData,
			ComplexityCalculator complexity) {
		double ccn = complexity.getCCNForProject(projectData);
		int numLinesCovered = projectData.getNumberOfCoveredLines();
		int numLinesValid = projectData.getNumberOfValidLines();
		int numBranchesCovered = projectData.getNumberOfCoveredBranches();
		int numBranchesValid = projectData.getNumberOfValidBranches();

		// TODO: Set a schema?
		//println("<coverage " + sourceDirectories.toString() + " xmlns=\"http://cobertura.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://cobertura.sourceforge.net/xml/coverage.xsd\">");
		println("<coverage line-rate=\"" + projectData.getLineCoverageRate()
				+ "\" branch-rate=\"" + projectData.getBranchCoverageRate()
				+ "\" lines-covered=\"" + numLinesCovered + "\" lines-valid=\""
				+ numLinesValid + "\" branches-covered=\"" + numBranchesCovered
				+ "\" branches-valid=\"" + numBranchesValid

				+ "\" complexity=\"" + ccn

				+ "\" version=\"" + Header.version() + "\" timestamp=\""
				+ new Date().getTime() + "\">");
	}

	protected void println(String ln) {
		indent();
		pw.println(ln);
	}

	protected void indent() {
		for (int i = 0; i < indent; i++) {
			pw.print("\t");
		}
	}

	protected void close() {
		pw.close();
	}

	protected void increaseIndentation() {
		indent++;
	}

	protected void decreaseIndentation() {
		if (indent > 0)
			indent--;
	}
}
