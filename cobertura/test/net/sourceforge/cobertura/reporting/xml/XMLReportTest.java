/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
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

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class XMLReportTest extends TestCase {

	private static final Logger LOGGER = Logger.getLogger(XMLReportTest.class);
	private final static String basedir = (System.getProperty("basedir") != null)
			? System.getProperty("basedir")
			: ".";
	private final static String pathToTestOutput = basedir
			+ "/build/test/XMLReportTest";
	private final static String pathToXMLReport = pathToTestOutput
			+ "/coverage.xml";
	private final static String pathToSourceCode = basedir + "/src";
	private final static String pathToCommandFile = "commandfile.txt";
	private File tmpDir;

	public void setUp()
	{
		tmpDir = new File(pathToTestOutput);
		tmpDir.mkdirs();
	}

	public void tearDown()
	{
		tmpDir = new File(pathToTestOutput);
		File files[] = tmpDir.listFiles();
		for (int i = 0; i < files.length; i++)
			files[i].delete();
		tmpDir.delete();
	}

	public void testXMLReportValidity() throws Exception {
//		String[] args;
//
//		// Serialize the current coverage data to disk
//		ProjectData.saveGlobalProjectData();
//		String dataFileName = CoverageDataFileHandler.getDefaultDataFile().getAbsolutePath();
//		// Then we need to generate the XML report
//		args = new String[] { "--commandsfile", pathToCommandFile };
//		try {
//			net.sourceforge.cobertura.reporting.Main.main(args);
//		} catch(Exception ex) {
//			System.err.println("Error running XML report test: " + ex.getMessage());
//			StringWriter writer = new StringWriter();
//			ex.printStackTrace(new PrintWriter(writer));
//			fail(ex.getMessage() + "\n" + writer.toString());
//		}
//
//		// Create a validating XML document parser
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		factory.setValidating(true);
//		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
//		documentBuilder.setEntityResolver(new JUnitXMLParserEntityResolver(
//				basedir));
//		documentBuilder.setErrorHandler(new JUnitXMLParserErrorHandler());
//
//		// Parse the XML report
//		InputStream inputStream = null;
//		try {
//			inputStream = new FileInputStream(pathToXMLReport);
//			documentBuilder.parse(inputStream);
//		} catch(Exception ex) {
//			LOGGER.error("Error testing XML report: " + ex.getMessage(), ex);
//		} finally {
//			if (inputStream != null)
//				inputStream.close();
//		}
	}

}