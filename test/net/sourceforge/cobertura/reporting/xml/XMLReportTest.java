/*
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

package net.sourceforge.cobertura.reporting.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coverage.CoverageDataFactory;

public class XMLReportTest extends TestCase
{

	private final static String basedir = (System.getProperty("basedir") != null)
			? System.getProperty("basedir")
			: ".";
	private final static String pathToSerFile = basedir
			+ "/build/test/cobertura.ser";
	private final static String pathToTestOutput = basedir
			+ "/build/test/XMLReportTest";
	private final static String pathToSourceCode = basedir + "/src";
	private File tmpDir;

	public void setUp()
	{
		tmpDir = new File(pathToTestOutput);
		tmpDir.mkdirs();
	}

	public void TODOtearDown()
	{
		tmpDir = new File(pathToTestOutput);
		File files[] = tmpDir.listFiles();
		for (int i = 0; i < files.length; i++)
			files[i].delete();
		tmpDir.delete();
	}

	public void testXMLReportValidity() throws Exception
	{
		String[] args;

		// This is a hacky way to save the ser file
		CoverageDataFactory coverageDataFactory = CoverageDataFactory
				.getInstance();
		coverageDataFactory.run();

		// Then we need to generate the XML report
		args = new String[] { "-f", "xml", "-i", pathToSerFile, "-o",
				pathToTestOutput, "-s", pathToSourceCode };
		net.sourceforge.cobertura.reporting.Main.main(args);

		// Now that that's out of the way, we can validate the XML report
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//factory.setValidating(true);
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		documentBuilder.setErrorHandler(new JUnitXMLParserErrorHandler());

		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(pathToTestOutput
					+ "/coverage.xml");
			documentBuilder.parse(inputStream);
		}
		finally
		{
			if (inputStream != null)
				inputStream.close();
		}
	}

}