/*
 * The MIT License
 * 
 * Copyright (c) 2018 msiemczyk
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.sourceforge.cobertura.bugs;

import java.io.IOException;

import org.junit.Test;

import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;

/**
 * @author msiemczyk
 * @see https://github.com/cobertura/cobertura/issues/379
 */
public class GithubIssue379Test extends AbstractCoberturaTestCase {
    
    @Test
    public void testIssue379() throws IOException
    {
        String imports = "import java.sql.Connection;"
            + "\nimport java.sql.PreparedStatement;"
            + "\nimport java.sql.SQLException;"
            + "\nimport java.sql.DriverManager;";
        String method =
            "\n public void semicolonAtTheEndOfTryWithResources() throws SQLException {"
                + "\n  Connection connection = DriverManager.getConnection(\"someUrl\");"
                + "\n"
                + "\n  try (PreparedStatement selectStatement = connection.prepareStatement(\"SELECT \");"
                + "\n      PreparedStatement insertStatement = connection.prepareStatement(\"INSERT \");) {"
                + "\n"
                + "\n    selectStatement.setString(1, \"some.parameter.value\");"
                + "\n  }"
                + "\n}";

        parseIssueTester(imports, method);
    }
}
