/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
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

package net.sourceforge.cobertura.coverage;

import java.io.File;

import junit.framework.TestCase;

public class InstrumentTest extends TestCase implements HasBeenInstrumented
{

	private final static String className = "HelloWorld";
	private final static String pathToTestInputClass = "build/test/classes/"
			+ className + ".class";
	private final static String pathToTestOutput = "build/test/InstrumentTest";
	private final static int[] validLines = { 37, 38, 39, 41, 45, 47, 49, 50,
			51, 55, 57, 60, 63, 66, 68, 72, 73, 74 };
	private final static int[] validBranches = { 38, 43, 47, 55, 60, 62, 64 };
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

	public void testInstrument()
	{
		System.setProperty("net.sourceforge.cobertura.rawcoverage.dir",
				pathToTestOutput);
		String[] args = new String[] { "-d", pathToTestOutput,
				pathToTestInputClass };
		Main.main(args);

		CoverageDataFactory coverageDataFactory = CoverageDataFactory
				.getInstance();
		coverageDataFactory.run(); // This is a hacky way to save the ser file
		CoverageData coverageData = coverageDataFactory
				.getInstrumentation(className);

		assertEquals(validLines.length, coverageData.getNumberOfValidLines());
		for (int i = 0; i < validLines.length; i++)
			assertTrue("Line " + validLines[i]
					+ " should be considered valid!", coverageData
					.isValidSourceLineNumber(validLines[i]));

		//assertEquals(validBranches.length, coverageData.getNumberOfValidBranches());
		//for (int i = 0; i < validBranches.length; i++)
		//	assertTrue("Line " + validBranches[i] + " should be considered valid!", coverageData.isValidSourceLineNumber(validBranches[i]));
	}

}