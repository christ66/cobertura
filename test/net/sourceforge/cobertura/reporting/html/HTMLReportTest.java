/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Grzegorz Lukasik
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.xml.JUnitXMLParserErrorHandler;
import net.sourceforge.cobertura.reporting.xml.XMLReport;

public class HTMLReportTest extends TestCase
{

	private final static File BASEDIR = new File((System.getProperty("basedir") != null)
			? System.getProperty("basedir")
			: ".");
	private final static File PATH_TO_TEST_OUTPUT = new File(BASEDIR,"build/test/HTMLReportTest");
	private final static File PATH_TO_SOURCES = new File(BASEDIR, "src");
	private final static File PATH_TO_SOURCES_2 = new File(BASEDIR, "src-2");

	public void setUp()
	{
		removeDir(PATH_TO_TEST_OUTPUT);
		PATH_TO_TEST_OUTPUT.mkdirs();
	}

	private void removeDir(File dir) {
		File files[] = dir.listFiles();
		if( files==null)
			return;
		
		for (int i = 0; i < files.length; i++) {
			if( files[i].isDirectory())
				removeDir(files[i]);
			files[i].delete();
		}
		dir.delete();
	}
	
	public void tearDown()
	{
		// Do do not remove results so that if an error occur we will be able
		// to localize the problem
		// removeDir( PATH_TO_TEST_OUTPUT);
	}
	
	private void validateXML(File pathToXML) throws Exception {
		// Create a validating XML document parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		documentBuilder.setErrorHandler(new JUnitXMLParserErrorHandler());

		// Parse the XML report
		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(pathToXML);
			documentBuilder.parse(inputStream);
		} catch( Throwable th) {
			th.printStackTrace();
			fail( "Cannot validate [" + pathToXML.getName() + "] file\n" + th.getMessage());
		}
		finally
		{
			if (inputStream != null)
				inputStream.close();
		}
	}

	private boolean containsFile( String[] files, String fileName) {
		for( int i=0; i<files.length; i++) {
			if( files[i].equals(fileName))
				return true;
		}
		return false;
	}
	
	public void testHTMLReportValidity() throws Exception
	{
		// Adding line to the project data that hasn't been yet marked as source line 
		ClassData cd = ProjectData.getGlobalProjectData().getOrCreateClassData(XMLReport.class.getName());
		cd.touch(7777);
		
		// Serialize the current coverage data to disk
		ProjectData.saveGlobalProjectData();
		String dataFileName = CoverageDataFileHandler.getDefaultDataFile()
				.getAbsolutePath();

		// Then we need to generate the HTML report
		String[] 
		args = new String[] { "--format", "html", "--datafile", dataFileName, "--destination",
				PATH_TO_TEST_OUTPUT.getAbsolutePath(), 
				PATH_TO_SOURCES.getAbsolutePath(), PATH_TO_SOURCES_2.getAbsolutePath() };
		net.sourceforge.cobertura.reporting.Main.main(args);
		
		// Get all files from report directory
		String htmlFiles[] = PATH_TO_TEST_OUTPUT.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".html");
			};
		});
		Arrays.sort(htmlFiles);

		assertTrue( htmlFiles.length>=5);
		
		// Assert that all required files are there
		String[] requiredFiles = { "index.html", "help.html", "frame-packages.html",
				"frame-summary.html", "frame-sourcefiles.html" };
		
		for( int i=0; i<requiredFiles.length; i++) {
			if( !containsFile( htmlFiles, requiredFiles[i])) {
				fail( "File " + requiredFiles[i] + " not found among report files");
			}
		}
			
		// Validate selected files
		String previousPrefix = "NONE";
		for( int i=0; i<htmlFiles.length; i++) {
			// Validate file if has prefix different than previous one, or is required file
			if( containsFile( requiredFiles, htmlFiles[i]) || !htmlFiles[i].startsWith(previousPrefix)) {
				System.out.println( "Validating " + htmlFiles[i]);
				validateXML( new File( PATH_TO_TEST_OUTPUT, htmlFiles[i]));
			}
			if( htmlFiles[i].length()>7) {
				previousPrefix = htmlFiles[i].substring(0,7);
			} else {
				previousPrefix = htmlFiles[i];
			}
		}
	}
}