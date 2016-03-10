package net.sourceforge.cobertura.bugs;

import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;
import org.junit.Test;

import java.io.IOException;

/**
 * @author stevenchristou
 *         Date: 3/10/16
 *         Time: 12:24 AM
 */
public class GithubIssue298 extends AbstractCoberturaTestCase {
    @Test
    public void testIssue298() throws IOException {
        String imports = "import java.io.*;" +
                "\n import java.lang.*;";
        String method =
                "\n public class TestParse298 {"
                        + "\n   private @Deprecated transient String fieldName;"
                        + "\n   public void test298() {"
                        + "\n   }"
                        + "\n }";

        parseIssueTester(imports, method);
    }

    @Test
    public void testIssue298Part2() throws IOException {
        String imports = "import java.io.*;" +
                "\n import java.lang.*;";
        String method =
                "\n public class TestParse298 {"
                        + "\n   private transient @Deprecated String fieldName;"
                        + "\n   public void test298() {"
                        + "\n   }"
                        + "\n }";

        parseIssueTester(imports, method);
    }
}
