
package net.sourceforge.cobertura.reporting.xml;

import junit.framework.Assert;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * This is a very simple SAX XML parser ErrorHandler.  If
 * you are parsing an XML document using a DocumentBuilder,
 * and you set the DocumentBuilder's ErrorHandler to an
 * instance of this class, and the XML document contains
 * any suspect XML, then this class will throw a JUnit
 * assertion failure.
 */
public class JUnitXMLParserErrorHandler implements ErrorHandler
{

	public void error(SAXParseException exception)
	{
		Assert.fail(exception.toString());
	}

	public void fatalError(SAXParseException exception)
	{
		Assert.fail(exception.toString());
	}

	public void warning(SAXParseException exception)
	{
		Assert.fail(exception.toString());
	}

}
