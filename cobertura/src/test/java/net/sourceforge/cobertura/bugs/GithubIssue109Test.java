package net.sourceforge.cobertura.bugs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;
import org.junit.Test;

/**
 * Testing the parse issue with a SuppressWarnings annotation inside a for loop declaration.
 * 
 * @author jad007
 */
public class GithubIssue109Test extends AbstractCoberturaTestCase
{
	@Test
	public void testIssue109() throws IOException
	{
		
		String imports = "import java.util.ArrayList;"
			+ "\n import java.util.Collection;"
			+ "\nimport java.util.Iterator;";
		String method =
			"\n public void suppressWarningsInForLoop() {"
				+ "\n  Collection<String> c = new ArrayList<String>();"
				+ "\n  for (@SuppressWarnings(\"unchecked\")"
				+ "\n    Iterator<String> it = c.iterator(); it.hasNext();) {"
				+ "\n    System.out.println(it.next());"
				+ "\n  }"
				+ "\n}";

		parseIssueTester(imports, method);
	}
}