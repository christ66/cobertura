/*
 * The MIT License
 * 
 * Copyright (c) 2014 schristou88
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

import groovy.util.Node;
import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author schristou88
 */
public class GithubIssue170 extends AbstractCoberturaTestCase {
    Node dom;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dom = super.createAndExecuteMainMethod(packageName, fileName, file1,
                mainMethod);
    }

    @Test
    public void testIssue() {
        assertTrue(TestUtils.getTotalHitCount(dom,
                "mypackage.GithubIssue170Test",
                "test170") > 0);
        assertEquals(3, TestUtils.getTotalHitCount(dom,
                "mypackage.GithubIssue170Test",
                "main"));
        assertEquals(2, TestUtils.getTotalHitCount(dom, "mypackage.GithubIssue170Test",
                "<init>"));
    }

    static final String packageName = "mypackage";
    static final String fileName = "GithubIssue170Test";
    static final String mainMethod = "mypackage.GithubIssue170Test";
    static final String file1 = "package mypackage;"
            + "\n public class GithubIssue170Test {"
            + "\n "
            + "\n   public void cleanUp() {}"
            + "\n   public void possiblyThrowAnException(int y) {"
            + "\n     try {"
            + "\n      int x = 1 / y;"
            + "\n     } catch (java.lang.Exception e) {"
            + "\n       throw e;"
            + "\n     }"
            + "\n   }"
            + "\n   public void test170(int y) {"
            + "\n     boolean error = true;"
            + "\n     try {"
            + "\n       possiblyThrowAnException(y);"
            + "\n       error = false;"
            + "\n      } finally {"
            + "\n       if (error)"
            + "\n          cleanUp();"
            + "\n     }"
            + "\n   }"
            + "\n   public static void main(String[] args) {"
            + "\n     try {"
            + "\n     new GithubIssue170Test().test170(1);"
            + "\n     new GithubIssue170Test().test170(0);"
            + "\n      } catch (java.lang.Exception e){"
            + "\n        // Ignore exceptions"
            + "\n     }"
            + "\n   }"
            + "\n }";
}
