package net.sourceforge.cobertura.bugs;

import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;
import org.junit.Test;

import java.io.IOException;

/**
 * @author schristou88
 */
public class GithubIssue191Test extends AbstractCoberturaTestCase {
    @Test
    public void testIssue191() throws IOException {
      String imports = "import java.io.*;" +
                    "\n import java.lang.*;";
      String method =
                "\n public class TestParse191 {"
              + "\n   public void test191() {"
              + "\n     try {new File(\"\").getCanonicalPath();"
              + "\n     } catch (NullPointerException | IOException | SecurityException e) {"
              + "\n     }"
              + "\n   }"
              + "\n }";

      super.parseIssueTester(imports, method);
    }
}
