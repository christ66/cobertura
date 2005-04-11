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

package net.sourceforge.cobertura.coveragedata;

import junit.framework.TestCase;

public class PackageDataTest extends TestCase
{

	private PackageData packageData;

	public void setUp()
	{
		packageData = new PackageData("com.example.HelloWorld");
	}

	public void testAddClass()
	{
		ClassData classData;

		assertEquals(0, packageData.getNumberOfClasses());

		classData = new ClassData("HelloWorld");
		classData.setSourceFileName("com/example/HelloWorld.java");
		for (int i = 0; i < 10; i++)
			classData.addLine(i, "test", "(I)B");
		packageData.addClassData(classData);
		assertEquals(1, packageData.getNumberOfClasses());

		classData = new ClassData("HelloWorldHelper");
		classData.setSourceFileName("com/example/HelloWorldHelper.java");
		for (int i = 0; i < 14; i++)
			classData.addLine(i, "test", "(I)B");
		packageData.addClassData(classData);
		assertEquals(2, packageData.getNumberOfClasses());

		// See what happens when we try to add the same class twice
		classData = new ClassData("HelloWorld");
		classData.setSourceFileName("com/example/HelloWorld.java");
		for (int i = 0; i < 19; i++)
			classData.addLine(i, "test", "(I)B");
		try
		{
			packageData.addClassData(classData);
			fail("Expected an IllegalArgumentException but did not receive one!");
		}
		catch (IllegalArgumentException e)
		{
			// Good!
		}

		assertEquals(2, packageData.getNumberOfClasses());
	}

	public void testEquals()
	{
		PackageData a = new PackageData("com.example.HelloWorld");
		PackageData b = new PackageData("com.example.HelloWorld");
		PackageData c = new PackageData("com.example.HelloWorld");
		ClassData classData1 = new ClassData("HelloWorld1");
		ClassData classData2 = new ClassData("HelloWorld2");
		ClassData classData3 = new ClassData("HelloWorld3");
		ClassData classData4 = new ClassData("HelloWorld4");

		classData1.setSourceFileName("com/example/HelloWorld1.java");
		classData2.setSourceFileName("com/example/HelloWorld2.java");
		classData3.setSourceFileName("com/example/HelloWorld3.java");
		classData4.setSourceFileName("com/example/HelloWorld4.java");

		a.addClassData(classData1);
		a.addClassData(classData2);
		a.addClassData(classData3);
		b.addClassData(classData1);
		b.addClassData(classData2);
		c.addClassData(classData1);
		c.addClassData(classData2);
		c.addClassData(classData4);

		assertFalse(a.equals(null));
		assertFalse(a.equals(classData1));

		assertTrue(a.equals(a));
		assertFalse(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(b.equals(a));
		assertTrue(b.equals(b));
		assertFalse(b.equals(c));
		assertFalse(c.equals(a));
		assertFalse(c.equals(b));
		assertTrue(c.equals(c));

		b.addClassData(classData3);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));

		assertFalse(a.equals(c));
		assertFalse(c.equals(a));
	}
}
