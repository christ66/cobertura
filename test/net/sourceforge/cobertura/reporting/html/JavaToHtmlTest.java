/**
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

package net.sourceforge.cobertura.reporting.html;

import junit.framework.TestCase;

public class JavaToHtmlTest extends TestCase
{

	public static void testSyntaxHighlight()
	{
		JavaToHtml javaToHtml = new JavaToHtml();

		assertEquals(
				"<span class=\"keyword\">package</span> net.sourceforge.cobertura.reporting.html;",
				javaToHtml
						.process("package net.sourceforge.cobertura.reporting.html;"));
		assertEquals(
				"<span class=\"keyword\">import</span> junit.framework.TestCase;",
				javaToHtml.process("import junit.framework.TestCase;"));
		assertEquals(
				"<span class=\"keyword\">public</span> <span class=\"keyword\">class</span> javaToHtmlTest <span class=\"keyword\">extends</span> TestCase {",
				javaToHtml
						.process("public class javaToHtmlTest extends TestCase {"));
		assertEquals(
				"<span class=\"keyword\">public</span> <span class=\"keyword\">static</span> <span class=\"keyword\">void</span> testSyntaxHighlight() {",
				javaToHtml
						.process("public static void testSyntaxHighlight() {"));
		assertEquals(
				"System.out.println(javaToHtml.process(<span class=\"string\">\"\"</span>));",
				javaToHtml
						.process("System.out.println(javaToHtml.process(\"\"));"));
		assertEquals("}", javaToHtml.process("}"));
		assertEquals(" ", javaToHtml.process(" "));
		assertEquals("        ", javaToHtml.process("	"));
	}
}