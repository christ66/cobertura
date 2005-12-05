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
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.JUnitXMLParserEntityResolver;
import net.sourceforge.cobertura.reporting.JUnitXMLParserErrorHandler;
import net.sourceforge.cobertura.util.FileFinder;

public class XMLReportTest extends TestCase
{

	private final static String BASEDIR = (System.getProperty("basedir") != null)
			? System.getProperty("basedir")
			: ".";
	private final static String PATH_TO_TEST_OUTPUT = BASEDIR
			+ "/build/test/XMLReportTest";
	private final static String PATH_TO_XML_REPORT = PATH_TO_TEST_OUTPUT
			+ "/coverage.xml";
	private final static String PATH_TO_SOURCE_CODE = BASEDIR + "/src";
	private File tmpDir;

	public void setUp()
	{
		tmpDir = new File(PATH_TO_TEST_OUTPUT);
		tmpDir.mkdirs();
	}

	public void tearDown()
	{
		tmpDir = new File(PATH_TO_TEST_OUTPUT);
		File files[] = tmpDir.listFiles();
		for (int i = 0; i < files.length; i++)
			files[i].delete();
		tmpDir.delete();
	}
	
	private void validateReport(String pathToXMLReport) throws Exception {
		// Create a validating XML document parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		documentBuilder.setEntityResolver(new JUnitXMLParserEntityResolver(
				new File(BASEDIR, "/etc/dtds")));
		documentBuilder.setErrorHandler(new JUnitXMLParserErrorHandler());

		// Parse the XML report
		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(pathToXMLReport);
			documentBuilder.parse(inputStream);
		}
		finally
		{
			if (inputStream != null)
				inputStream.close();
		}
	}

	public void testXMLReportValidity() throws Exception
	{
		String[] args;

		// Serialize the current coverage data to disk
		ProjectData.saveGlobalProjectData();
		String dataFileName = CoverageDataFileHandler.getDefaultDataFile()
				.getAbsolutePath();

		// Then we need to generate the XML report
		args = new String[] { "--format", "xml", "--datafile", dataFileName, "--destination",
				PATH_TO_TEST_OUTPUT, PATH_TO_SOURCE_CODE };
		net.sourceforge.cobertura.reporting.Main.main(args);
		
		validateReport(PATH_TO_XML_REPORT);
	}
	
	public void testXMLReportWithNonSourceLines() throws Exception {
		ProjectData projectData = new ProjectData();
		
		// Adding line to the project data that hasn't been yet marked as source line 
		ClassData cd = projectData.getOrCreateClassData(XMLReport.class.getName());
		cd.touch(7777);
		
		File reportDir = File.createTempFile( "XMLReportTest", "");
		reportDir.delete();
		reportDir.mkdir();
		
		FileFinder fileFinder = new FileFinder();
		ComplexityCalculator complexity = new ComplexityCalculator(fileFinder);
		
		new XMLReport( projectData, reportDir, fileFinder, complexity);
		
		File coverageFile = new File(reportDir,"coverage.xml");
		validateReport( coverageFile.getAbsolutePath());

		coverageFile.delete();
		reportDir.delete();
	}

}