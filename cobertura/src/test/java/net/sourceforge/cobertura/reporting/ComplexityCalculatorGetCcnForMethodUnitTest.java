/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2014 Kunal Shah
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
package net.sourceforge.cobertura.reporting;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.javancss.FunctionMetric;
import net.sourceforge.cobertura.javancss.Javancss;
import net.sourceforge.cobertura.javancss.JavancssFactory;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Source;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link ComplexityCalculator}.
 *
 * @author Kunal Shah
 */
public class ComplexityCalculatorGetCcnForMethodUnitTest {
	private static final String METHOD_TEMPLATE = "%s(%s)";
	private static final ClassData CLASS_DATA = new ClassData(Klass.class.getName());
	private static final ClassData INNER_CLASS_DATA = new ClassData(Klass.InnerKlass.class.getName());
	private static final ClassData GENERIC_CLASS_DATA = new ClassData(KlassToTestGenericType.class.getName());
	private static final String CLASS_CONSTRUCTOR_NAME_FQ = CLASS_DATA.getName() + "." + CLASS_DATA.getBaseName();
	private static final String INNER_CLASS_CONSTRUCTOR_NAME_FQ = (INNER_CLASS_DATA.getName() + "." + INNER_CLASS_DATA.getBaseName()).replaceAll(Pattern.quote("$"), ".");
	private static final String METHOD_1_NAME = "method1";
	private static final String METHOD_1_NAME_FQ = CLASS_DATA.getName() + "." + METHOD_1_NAME;
	private static final String METHOD_2_NAME = "method2";
	private static final String METHOD_2_NAME_FQ = CLASS_DATA.getName() + "." + METHOD_2_NAME;
	private static final String METHOD_3_NAME_FQ = INNER_CLASS_DATA.getName().replaceAll(Pattern.quote("$"), ".") + "." + METHOD_1_NAME;
	private static final String METHOD_1_NAME_GENERIC_FQ = GENERIC_CLASS_DATA.getName().replaceAll(Pattern.quote("$"), ".") + "." + METHOD_1_NAME;

	private static JavancssFactory javancssFactory;
	private static FileFinder fileFinder;
	private static ComplexityCalculator complexityCalculator;


	@BeforeClass
	public static void setUp() {

		// mock fileFinder
		fileFinder = mock(FileFinder.class);
		Source source = mock(Source.class);
		when(fileFinder.getSource(any(String.class))).thenReturn(source);

		// mock javancssFactory
		javancssFactory = mock(JavancssFactory.class);
		Javancss javancss = mock(Javancss.class);
		List<FunctionMetric> functionMetrics = Arrays.asList(new FunctionMetric[]{
			newFunctionMetric(METHOD_1_NAME_FQ, "", 1),
			newFunctionMetric(METHOD_1_NAME_FQ, "long", 2),
			newFunctionMetric(METHOD_1_NAME_FQ, "List<Integer>", 3),
			newFunctionMetric(METHOD_1_NAME_FQ, "java.util.List<Integer>, Integer[]", 4),
			newFunctionMetric(METHOD_1_NAME_FQ, "int[]", 5),
			newFunctionMetric(METHOD_1_NAME_FQ, "String[][]", 6),
			newFunctionMetric(METHOD_1_NAME_FQ, "Map<String, List<String>>", 7),
			newFunctionMetric(METHOD_1_NAME_FQ, "Long", 8),
			newFunctionMetric(METHOD_1_NAME_FQ, "AtomicLong", 9),
			newFunctionMetric(METHOD_1_NAME_FQ, "Long[]", 10),
			newFunctionMetric(METHOD_1_NAME_FQ, "int[][][], String", 11),
			newFunctionMetric(CLASS_CONSTRUCTOR_NAME_FQ, "", 12),
			newFunctionMetric(CLASS_CONSTRUCTOR_NAME_FQ, "boolean", 13),
			newFunctionMetric(METHOD_2_NAME_FQ, "int[][][], String", 14),
			newFunctionMetric(METHOD_3_NAME_FQ, "Long[]", 15),
			newFunctionMetric(METHOD_3_NAME_FQ, "long", 16),
			newFunctionMetric(INNER_CLASS_CONSTRUCTOR_NAME_FQ, "boolean", 17),

			newFunctionMetric(METHOD_1_NAME_GENERIC_FQ, "E", 18),
			newFunctionMetric(METHOD_1_NAME_GENERIC_FQ, "int, Object", 19),
			newFunctionMetric(METHOD_1_NAME_GENERIC_FQ, "int, E[]", 20),
			newFunctionMetric(METHOD_1_NAME_GENERIC_FQ, "int, Map<E, List<? extends E>>", 21),
			newFunctionMetric(METHOD_1_NAME_GENERIC_FQ, "List<T>, T", 22),
		});
		when(javancss.getFunctionMetrics()).thenReturn(functionMetrics);
		when(javancssFactory.newInstance(any(InputStream.class), any(String.class))).thenReturn(javancss);

		complexityCalculator = new ComplexityCalculator(fileFinder, javancssFactory);
	}

	@Test
	public void GIVEN_methodDoesNotExist_WHEN_getCCNForMethod_THEN_zeroCcn() {
		int expectedCcn = 0;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, "doesNotExist", "()V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithNoParameters_THEN_correctCcn() {
		int expectedCcn = 1;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "()V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithPrimitiveParameter_THEN_correctCcn() {
		int expectedCcn = 2;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "(J)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithParameterizedTypeParameter_THEN_correctCcn() {
		int expectedCcn = 3;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "(Ljava/util/List;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithFullyQualifiedParameterizedTypeAndReferenceVarargsParameters_THEN_correctCcn() {
		int expectedCcn = 4;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "(Ljava/util/List;[Ljava/lang/Integer;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithPrimitiveArrayParameter_THEN_correctCcn() {
		int expectedCcn = 5;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "([I)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithReferenceMultipleArrayVarargsParameter_THEN_correctCcn() {
		int expectedCcn = 6;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "([[Ljava/lang/String;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithParameterizedTypeOfParameterizedTypeParameter_THEN_correctCcn() {
		int expectedCcn = 7;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "(Ljava/util/Map;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithBoxedPrimitiveParameter_THEN_correctCcn() {
		int expectedCcn = 8;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "(Ljava/lang/Long;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithBoxedPrimitiveLikeParameter_THEN_correctCcn() {
		int expectedCcn = 9;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "(Ljava/util/concurrent/atomic/AtomicLong;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithBoxedPrimitiveArrayParameter_THEN_correctCcn() {
		int expectedCcn = 10;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "([Ljava/lang/Long;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithPrimitiveMultiArrayAndReferenceParameter_THEN_correctCcn() {
		int expectedCcn = 11;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_1_NAME, "([[[ILjava/lang/String;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_constructorExists_WHEN_getCCNForMethodWithConstructorAndNoParameters_THEN_correctCcn() {
		int expectedCcn = 12;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, "<init>", "()V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_constructorExists_WHEN_getCCNForMethodWithConstructorAndSingleParameter_THEN_correctCcn() {
		int expectedCcn = 13;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, "<init>", "(Z)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_nonOverloadedMethodExists_WHEN_getCCNForMethodWithParameterSameAsOtherMethod_THEN_correctCcn() {
		int expectedCcn = 14;
		int actualCcn = complexityCalculator.getCCNForMethod(CLASS_DATA, METHOD_2_NAME, "([[[ILjava/lang/String;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExistsInInnerClass_WHEN_getCCNForMethodWithSignatureSameAsEnclosingClass_THEN_correctCcn() {
		int expectedCcn = 15;
		int actualCcn = complexityCalculator.getCCNForMethod(INNER_CLASS_DATA, METHOD_1_NAME, "([Ljava/lang/Long;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_constructorExistsInInnerClass_WHEN_getCCNForMethodWithConstructorAndSingleParameter_THEN_correctCcn() {
		int expectedCcn = 17;
		int actualCcn = complexityCalculator.getCCNForMethod(INNER_CLASS_DATA, "<init>", "(Z)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithGenericTypeParameter_THEN_correctCcn() {
		int expectedCcn = 18;
		int actualCcn = complexityCalculator.getCCNForMethod(GENERIC_CLASS_DATA, METHOD_1_NAME, "(Ljava/lang/Object;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithPrimitveAndErasedTypeParameterEquivalentParameters_THEN_correctCcn() {
		int expectedCcn = 19;
		int actualCcn = complexityCalculator.getCCNForMethod(GENERIC_CLASS_DATA, METHOD_1_NAME, "(ILjava/lang/Object;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithPrimitveAndTypeParameterVarargsParameters_THEN_correctCcn() {
		int expectedCcn = 20;
		int actualCcn = complexityCalculator.getCCNForMethod(GENERIC_CLASS_DATA, METHOD_1_NAME, "(I[Ljava/lang/Object;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_methodExists_WHEN_getCCNForMethodWithPrimitveAndGenericTypeParameters_THEN_correctCcn() {
		int expectedCcn = 21;
		int actualCcn = complexityCalculator.getCCNForMethod(GENERIC_CLASS_DATA, METHOD_1_NAME, "(ILjava/util/Map;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	@Test
	public void GIVEN_staticMethodExists_WHEN_getCCNForMethodWithGenericTypeParameter_THEN_correctCcn() {
		int expectedCcn = 22;
		int actualCcn = complexityCalculator.getCCNForMethod(GENERIC_CLASS_DATA, METHOD_1_NAME, "(Ljava/util/List;Ljava/lang/Comparable;)V");
		assertEquals(expectedCcn, actualCcn);
	}

	private static FunctionMetric newFunctionMetric(String methodName, String methodSignature, int ccn) {
		FunctionMetric functionMetric = new FunctionMetric();
		functionMetric.name = String.format(METHOD_TEMPLATE, methodName, methodSignature);
		functionMetric.ccn = ccn;
		return functionMetric;
	}

}

/**
 * Class used for the unit tests in {@link ComplexityCalculatorGetCcnForMethodUnitTest}.
 */
class Klass {
	Klass() {}
	Klass(boolean a) {}
	private void method1() {}
	void method1(long a) {}
	public void method1(List<Integer> a) {}
	void method1(List<Integer> a, Integer... b) {}
	private void method1(int[] a) {}
	private void method1(String[]... a) {}
	void method1(Map<String, List<String>> a) {}
	public void method1(Long a) {}
	private void method1(AtomicLong a) {}
	void method1(Long... a) {}
	public void method1(int[][][] a, String b) {}
	public void method2(int[][][] a, String b) {}

	class InnerKlass {
		InnerKlass(boolean a) {}
		private void method1(long a) {}
		void method1(Long... a) {}
	}
}

/**
 * Class used for the unit tests in {@link ComplexityCalculatorGetCcnForMethodUnitTest}.
 */
class KlassToTestGenericType<E> {
	private void method1(E a) {}
	private void method1(int a, java.lang.Object b) {}
	private void method1(int a, E... b) {}
	private void method1(int a, Map<E, List<? extends E>> b) {}
	private static <T extends Comparable<T>> void method1(List<T> a, T b) {}
}