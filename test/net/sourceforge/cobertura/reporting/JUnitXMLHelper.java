package net.sourceforge.cobertura.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class JUnitXMLHelper
{

	private final static String BASEDIR = (System.getProperty("basedir") != null) ? System
			.getProperty("basedir") : ".";

	public static void validate(File file) throws FileNotFoundException, IOException,
			ParserConfigurationException, SAXException
	{
		System.out.println("Validating " + file.getAbsolutePath());

		// Create a validating XML document parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		documentBuilder.setEntityResolver(new JUnitXMLParserEntityResolver(new File(BASEDIR,
				"etc/dtds")));
		documentBuilder.setErrorHandler(new JUnitXMLParserErrorHandler());

		// Parse the XML report
		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(file);
			documentBuilder.parse(inputStream);
		}
		finally
		{
			if (inputStream != null)
				inputStream.close();
		}
	}

}
