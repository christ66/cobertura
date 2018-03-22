/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (C) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 * Copyright (C) 2008 John Lewis
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package net.sourceforge.cobertura.reporting;

import junit.framework.TestCase;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.test.util.TestUtils;
import net.sourceforge.cobertura.util.FileFinder;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

public class ComplexityCalculator2Test extends TestCase {
	@Test
	public void testSearchJarsForSourceInJar() throws Exception {
		File tempDir = TestUtils.getTempDir();
		File zipFile = TestUtils.createSourceArchive(tempDir);

		//create a ComplexityCalculator that will use the archive
		FileFinder fileFinder = new FileFinder();
		fileFinder
				.addSourceDirectory(zipFile.getParentFile().getAbsolutePath());
		ComplexityCalculator complexity = new ComplexityCalculator(fileFinder);

		double ccn1 = complexity.getCCNForSourceFile(new SourceFileData(
				TestUtils.SIMPLE_SOURCE_PATHNAME));
		assertTrue(ccn1 == 1.0);
	}

	public void testAnnotatedSource() throws Exception {
		/*
		 * Test for bug #2818738.
		 */
		File tempDir = TestUtils.getTempDir();
		String filename = "TBSException.java";
		File sourceFile = new File(tempDir, filename);
		FileUtils
				.write(
						sourceFile,
						"\n public class TBSException extends Exception {"
								+ "\n public TBSException (ErrorHandler handler, Exception wrap) {"
								+ "\n super(wrap);"
								+ "\n @SuppressWarnings(\"unchecked\")"
								+ "\n final Iterator<Exception> iter = handler.getExceptions().iterator();  // LINE 27"
								+ "\n for (; iter.hasNext();) " + "\n {"
								+ "\n Exception exception = iter.next();"
								+ "\n this.errors.add(exception.getMessage());"
								+ "\n }" + "\n 	}" + "\n }");

		//create a ComplexityCalculator that will use the archive
		FileFinder fileFinder = new FileFinder();
		fileFinder.addSourceDirectory(tempDir.getAbsolutePath());
		ComplexityCalculator complexity = new ComplexityCalculator(fileFinder);

		double ccn1 = complexity.getCCNForSourceFile(new SourceFileData(
				filename));
		assertNotNull(ccn1);
		assertEquals(2.0, ccn1, 0.01);
	}

	/**
	 * This test highlights an issue with Javancss.
	 * 
	 * http://jira.codehaus.org/browse/JAVANCSS-37
	 * @throws Exception 
	 * 
	 */
	public void testGenericsProblem() throws Exception {
		File tempDir = TestUtils.getTempDir();
		String filename = "UserAudit.java";
		File sourceFile = new File(tempDir, filename);
		FileUtils
				.write(
						sourceFile,
						"\n import java.util.ArrayList;"
								+ "\n import java.util.List;"
								+ "\n "
								+ "\n "
								+ "\n public class UserAudit extends UserAuditParent {"
								+ "\n void postCopyOnDestination(String str) throws InstantiationException, IllegalAccessException {"
								+ "\n List<AllowedMMProduct> listToReset = new ArrayList<AllowedMMProduct>();"
								+ "\n"
								+ "\n List<AllowedMMProductAudit> auditProducts;"
								+ "\n auditProducts = this.<AllowedMMProduct,AllowedMMProductAudit>copyListFromParent(AllowedMMProductAudit.class, getMmAuthorisedProducts_());"
								+ "\n }"
								+ "\n"
								+ "\n List<AllowedMMProduct> getMmAuthorisedProducts_() {"
								+ "\n return null;" + "\n 	}" + "\n }");

		//create a ComplexityCalculator that will use the archive
		FileFinder fileFinder = new FileFinder();
		fileFinder.addSourceDirectory(tempDir.getAbsolutePath());
		ComplexityCalculator complexity = new ComplexityCalculator(fileFinder);

		double ccn1 = complexity.getCCNForSourceFile(new SourceFileData(
				filename));
		assertNotNull(ccn1);
		assertEquals(
				"Javancss issue has been fixed: http://jira.codehaus.org/browse/JAVANCSS-37.   Now fix this test.",
                    1.0, ccn1, 0.01);
    }

    /**
     * This test highlights an issue with Javancss not supporting java8 default method for interfaces.
     * <p>
     * @throws Exception
     * <p>
     */
    public void testJava8defaultAndStaticInterface() throws Exception {
        File tempDir = TestUtils.getTempDir();
        String filename = "Interface1.java";
        File sourceFile = new File(tempDir, filename);
        FileUtils
                .write(
                        sourceFile,
                        "\n interface Interface1 {"
                        + "\n static void staticTest(String str) {"
                        + "\n System.out.println(str);"
                        + "\n }"
                        + "\n"
                        + "\n default int defaultTest() {"
                        + "\n return 1;" + "\n 	}" + "\n }");

        //create a ComplexityCalculator that will use the archive
        FileFinder fileFinder = new FileFinder();
        fileFinder.addSourceDirectory(tempDir.getAbsolutePath());
        ComplexityCalculator complexity = new ComplexityCalculator(fileFinder);

        double ccn1 = complexity.getCCNForSourceFile(new SourceFileData(
                filename));
        assertNotNull(ccn1);
        assertEquals(
                "Testing default and static interface functions",
                1.0, ccn1, 0.01);
    }

    /**
     * This test highlights an issue with Javancss not supporting java8 function pointers and lambdas.
     * <p>
     * @throws Exception
     *                   <p>
     */
    public void testJava8functionPointersAndLambdas() throws Exception {
        File tempDir = TestUtils.getTempDir();
        String filename = "LamdasAndMethodRefs.java";
        File sourceFile = new File(tempDir, filename);
        FileUtils
                .write(
                        sourceFile,
                        "import java.util.ArrayList;\n"
                        + "import java.util.Arrays;\n"
                        + "import java.util.Iterator;\n"
                        + "import java.util.List;\n"
                        + "import java.util.function.Consumer;\n"
                        + "import java.util.function.Function;\n"
                        + "import java.util.function.IntBinaryOperator;\n"
                        + "import java.util.function.IntFunction;\n"
                        + "import java.util.function.IntSupplier;\n"
                        + "import java.util.function.LongSupplier;\n"
                        + "import java.util.function.Supplier;\n"
                        + "import java.util.function.ToIntFunction;\n"
                        + "import java.util.function.UnaryOperator;\n"
                        + "\n"
                        + "class MyList<E> extends ArrayList<E> {\n"
                        + "\n"
                        + "    public MyList replaceAll2(UnaryOperator<E> operator) {\n"
                        + "        return this;\n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "class T {\n"
                        + "\n"
                        + "    public void tvarMember() {\n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "class Foo {\n"
                        + "\n"
                        + "    public void bar() {\n"
                        + "    }\n"
                        + "\n"
                        + "    static class Bar {\n"
                        + "    }\n"
                        + "\n"
                        + "}\n"
                        + "\n"
                        + "class R<A> {\n"
                        + "\n"
                        + "    R() {\n"
                        + "    }\n"
                        + "\n"
                        + "    R(Integer a) {\n"
                        + "    }\n"
                        + "\n"
                        + "    R(String a) {\n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "public class LamdasAndMethodRefs {\n"
                        + "\n"
                        + "    public LamdasAndMethodRefs() {\n"
                        + "        //Method references\n"
                        + "        Runnable a = super::toString;\n"
                        + "        Runnable b = LamdasAndMethodRefs.super::toString;\n"
                        + "    }\n"
                        + "\n"
                        + "    public static void main(String[] args) {\n"
                        + "        //Method references\n"
                        + "        LongSupplier a = System::currentTimeMillis; // static method\n"
                        + "        ToIntFunction<String> b = String::length;             // instance method\n"
                        + "        ToIntFunction<List> c = List::size;\n"
                        + "        ToIntFunction<List<String>> d = List<String>::size;  // explicit type arguments for generic type\n"
                        + "        UnaryOperator<int[]> e = int[]::clone;\n"
                        + "        Consumer<T> f = T::tvarMember;\n"
                        + "\n"
                        + "        Runnable g = System.out::println;\n"
                        + "        Consumer<Integer> h = String::valueOf; // overload resolution needed\n"
                        + "        IntSupplier i = \"abc\"::length;\n"
                        + "        Consumer<int[]> j = Arrays::sort;          // type arguments inferred from context\n"
                        + "        Consumer<String[]> k = Arrays::<String>sort;          // explicit type arguments\n"
                        + "        Supplier<ArrayList<String>> l = ArrayList<String>::new;     // constructor for parameterized type\n"
                        + "        Supplier<ArrayList> m = ArrayList::new;             // inferred type arguments\n"
                        + "        IntFunction<int[]> n = int[]::new;                 // array creation\n"
                        + "        Supplier<Foo> o = Foo::<Integer>new; // explicit type arguments\n"
                        + "        Supplier<Foo.Bar> p = Foo.Bar::new;           // inner class constructor\n"
                        + "        Supplier<R<String>> q = R<String>::<Integer>new;  // generic class, generic constructor\n"
                        + "\n"
                        + "        Foo[] foo = new Foo[2];\n"
                        + "        int r = 1;\n"
                        + "        foo[r] = new Foo();\n"
                        + "        Runnable s = foo[r]::bar;\n"
                        + "        boolean test = false;\n"
                        + "        MyList<String> list = new MyList<>();\n"
                        + "        Supplier<Iterator<String>> fun = (test ? list.replaceAll2(String::trim) : list)::iterator;\n"
                        + "\n"
                        + "        // Lamdas\n"
                        + "        Runnable t = () -> {\n"
                        + "        }; // No parameters; result is void\n"
                        + "        IntSupplier u = () -> 42; // No parameters, expression body\n"
                        + "        Supplier<Object> v = () -> null; // No parameters, expression body\n"
                        + "        v = () -> {\n"
                        + "            return 42;\n"
                        + "        }; // No parameters, block body with return\n"
                        + "        t = () -> {\n"
                        + "            System.gc();\n"
                        + "        }; // No parameters, void block body\n"
                        + "        v = () -> {                 // Complex block body with returns\n"
                        + "            if (true) {\n"
                        + "                return 12;\n"
                        + "            }\n"
                        + "            else {\n"
                        + "                int result = 15;\n"
                        + "                for (int i2 = 1; i2 < 10; i2++) {\n"
                        + "                    result *= i2;\n"
                        + "                }\n"
                        + "                return result;\n"
                        + "            }\n"
                        + "        };\n"
                        + "        IntFunction<Integer> w = (int x) -> x + 1; // Single declared-type parameter\n"
                        + "        w = (int x) -> {\n"
                        + "            return x + 1;\n"
                        + "        }; // Single declared-type parameter\n"
                        + "        w = (x) -> x + 1; // Single inferred-type parameter\n"
                        + "        w = x -> x + 1; // Parentheses optional for\n"
                        + "                // single inferred-type parameter\n"
                        + "        Function<String, Integer> z = (String s2) -> s2.length(); // Single declared-type parameter\n"
                        + "        Consumer<Thread> a2 = (Thread t2) -> {\n"
                        + "            t2.start();\n"
                        + "        }; // Single declared-type parameter\n"
                        + "        z = s3 -> s3.length(); // Single inferred-type parameter\n"
                        + "        a2 = t3 -> {\n"
                        + "            t3.start();\n"
                        + "        }; // Single inferred-type parameter\n"
                        + "        IntBinaryOperator b2 = (int x, int y) -> x + y; // Multiple declared-type parameters\n"
                        + "        b2 = (x, y) -> x + y; // Multiple inferred-type parameters\n"
                        + "\n"
                        + "        List<String> myList\n"
                        + "                = Arrays.asList(\"a1\", \"a2\", \"b1\", \"c2\", \"c1\");\n"
                        + "\n"
                        + "        myList.stream().filter((s4) -> {\n"
                        + "            System.out.println(\"filter \" + s4);\n"
                        + "            return s4.startsWith(\"c\");\n"
                        + "        }).map(String::toUpperCase).sorted().forEach(\n"
                        + "                 System.out::println);\n"
                        + "\n"
                        + "    }\n"
                        + "\n"
                        + "}");

        //create a ComplexityCalculator that will use the archive
        FileFinder fileFinder = new FileFinder();
        fileFinder.addSourceDirectory(tempDir.getAbsolutePath());
        ComplexityCalculator complexity = new ComplexityCalculator(fileFinder);

        double ccn1 = complexity.getCCNForSourceFile(new SourceFileData(
                filename));
        assertNotNull(ccn1);
        assertEquals(
                "Testing method references and lambda functions",
                1.875, ccn1, 0.01);
    }

}