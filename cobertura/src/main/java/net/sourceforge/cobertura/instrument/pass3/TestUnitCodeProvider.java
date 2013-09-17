/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2013 Steven Christou
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License (so that it can be used from both the main
 * Cobertura classes and the ant tasks).
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

package net.sourceforge.cobertura.instrument.pass3;

import net.sourceforge.cobertura.Cobertura;
import net.sourceforge.cobertura.coveragedata.TestUnitInformationHolder;

import org.objectweb.asm.*;

/**
 * 
 * For every single line of code we add the following:
 * 
 * Before instrumentation:
 * 1: public int foo() {
 * 2:  int x = 0;
 * 3:  x++;
 * 4:  return x;
 * 5: }
 * 
 * After instrumentation:
 * public int foo() {
 *   __cobertura_counters
 *         .get(2)
 *         .append(", " + net.sourceforge.cobertura.Cobertura.TestClassAndMethodNamesMerged);
 *   int x = 0; 
 *   __cobertura_counters
 *         .get(3)
 *         .append(", " + net.sourceforge.cobertura.Cobertura.TestClassAndMethodNamesMerged);
 *   x++;
 *   __cobertura_counters
 *         .get(4)
 *         .append(", " + net.sourceforge.cobertura.Cobertura.TestClassAndMethodNamesMerged);
 *   return x;
 * }
 * 
 * In future versions it might be best to switch from the ConcurrentHashMap to a more static approach.
 *
 * @author christ66
 */
public class TestUnitCodeProvider extends AbstractCodeProvider
		implements
			CodeProvider, Opcodes {

	/**
	 * Type of the generated field, that is used to store counters
	 */
	static final String COBERTURA_COUNTERS_FIELD_TYPE = "[Lnet/sourceforge/cobertura/coveragedata/TestUnitInformationHolder;";

	/**
	 * Generates:
	 * 
	 */
	public void generateCodeThatIncrementsCoberturaCounterFromInternalVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex,
			String className) {
		nextMethodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		nextMethodVisitor.visitFieldInsn(Opcodes.GETFIELD, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
		nextMethodVisitor.visitInsn(Opcodes.DUP2);
		nextMethodVisitor.visitInsn(Opcodes.IALOAD);
		nextMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, Type
				.getInternalName(Cobertura.class),
				"TestClassAndMethodNamesMerged", "Ljava/lang/String;");
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type
				.getInternalName(TestUnitInformationHolder.class),
				"appendTestUnit", "(Ljava/lang/String;)V");
	}

	/**
	 * Generates:
	 * 
	 */
	public void generateCodeThatIncrementsCoberturaCounter(
			MethodVisitor nextMethodVisitor, Integer counterId, String className) {
		
		nextMethodVisitor.visitVarInsn(ALOAD, 0);
		nextMethodVisitor.visitFieldInsn(GETSTATIC,
										 className,
										 COBERTURA_COUNTERS_FIELD_NAME,
										 COBERTURA_COUNTERS_FIELD_TYPE);
		nextMethodVisitor.visitLdcInsn((int)counterId);
		nextMethodVisitor.visitInsn(AALOAD);
		nextMethodVisitor.visitFieldInsn(GETSTATIC,
										 "net/sourceforge/cobertura/Cobertura",
										 "TestClassAndMethodNamesMerged",
										 "Ljava/lang/String;");
		nextMethodVisitor.visitMethodInsn(INVOKEVIRTUAL,
										  "net/sourceforge/cobertura/coveragedata/TestUnitInformationHolder",
										  "appendTestUnit",
										  "(Ljava/lang/String;)V");
	}
	
	/**
	 * Generates:
	 * public static final transient Map __cobertura_counters;
	 */
	public void generateCountersField(ClassVisitor cv) {
		FieldVisitor fv = cv.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC
				| Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE,
				null, null);
		fv.visitEnd();
	}
	
	/**
	 * Generates:
	 *   public static void __cobertura_init()
	 *   {
	 *     if (__cobertura_counters == null)
	 *       {
	 *         __cobertura_counters = new TestUnitInformationHolder[class.length];
	 *       }
	 *       TouchCollector.registerClass("mypackage/HelloWorld");
	 *   }
	 */
	public void generateCINITmethod(MethodVisitor mv, String className,
			int counters_cnt) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		Label l1 = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, l1);
		mv.visitLdcInsn(counters_cnt);
		mv.visitTypeInsn(Opcodes.ANEWARRAY, Type
				.getInternalName(TestUnitInformationHolder.class));
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 1);
		Label l11 = new Label();
		mv.visitLabel(l11);
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLineNumber(24, l3);
		mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
		mv.visitFieldInsn(GETSTATIC, className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitVarInsn(ILOAD, 1);
		mv.visitTypeInsn(NEW, "net/sourceforge/cobertura/coveragedata/TestUnitInformationHolder");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "net/sourceforge/cobertura/coveragedata/TestUnitInformationHolder", "<init>", "()V");
		mv.visitInsn(AASTORE);
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitLineNumber(23, l4);
		mv.visitIincInsn(1, 1);
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ILOAD, 1);
		mv.visitFieldInsn(GETSTATIC, className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitInsn(ARRAYLENGTH);
		mv.visitJumpInsn(IF_ICMPLT, l3);

		
		mv.visitLabel(l1);

		generateRegisterClass(mv, className);
	}
	
	/**
	 * Generates:
	 *   public static TestUnitInformationHolder[] __cobertura_get_and_reset_counters()
	 *   {
	 *     TestUnitInformationHolder[] local = __cobertura_counters;
	 *     __cobertura_counters = new TestUnitInformationHolder[__cobertura_counters.length];
	 *     return local;
	 *   }
	 */
	public void generateCoberturaGetAndResetCountersMethod(ClassVisitor cv,
			String className) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC
				| Opcodes.ACC_STATIC,
				COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME, "()"
						+ COBERTURA_COUNTERS_FIELD_TYPE, null, null);
		mv.visitCode();
		/*cobertura_counters.*/
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitVarInsn(Opcodes.ASTORE, 0);
		/*cobertura_counters.*/
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitInsn(Opcodes.ARRAYLENGTH);

		mv.visitTypeInsn(Opcodes.ANEWARRAY, Type
				.getInternalName(TestUnitInformationHolder.class));
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(0, 0);//will be recalculated by writer
		mv.visitEnd();
	}
}
