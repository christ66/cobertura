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
import java.io.FileNotFoundException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * This is a very simple XML EntityResolver.  If
 * you are parsing an XML document using a DocumentBuilder,
 * and you set the DocumentBuilder's EntityResolver to an
 * instance of this class, and the XML document sets its
 * DOCTYPE to http://cobertura.sourceforge.net/xml/coverage.dtd, 
 * then instead of using the given URL, this class will
 * resolve the entity to the local version of the same file.
 * </p>
 *
 * <p>
 * This is done so that the XMLReportTest.java JUnit test will
 * not fail when the test is run on a non-networked machine,
 * or when webpages must be accessed through a proxy server.
 * </p>
 */
public class JUnitXMLParserEntityResolver extends DefaultHandler
{
	private final static String coverageDTD = "http://cobertura.sourceforge.net/xml/coverage.dtd";

	private final File localDTD;

	public JUnitXMLParserEntityResolver(String basedir)
	{
		localDTD = new File(basedir + "/etc/coverage.dtd");
	}

	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException
	{
		// If the requested entity is our cobertura DTD, then use the local
		// version instead of the remote version
		try
		{
			if (systemId.equals(coverageDTD))
				return new InputSource(new FileInputStream(localDTD));
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Unable to open local DTD file "
					+ localDTD.getAbsolutePath() + ", using " + systemId
					+ " instead.");
		}

		return super.resolveEntity(publicId, systemId);
	}

}