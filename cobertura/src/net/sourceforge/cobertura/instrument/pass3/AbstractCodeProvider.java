/*
 * Cobertura - http://cobertura.sourceforge.net/
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

import java.util.Collection;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.LightClassmapListener;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.instrument.tp.ClassMap;
import net.sourceforge.cobertura.instrument.tp.JumpTouchPointDescriptor;
import net.sourceforge.cobertura.instrument.tp.LineTouchPointDescriptor;
import net.sourceforge.cobertura.instrument.tp.SwitchTouchPointDescriptor;
import net.sourceforge.cobertura.instrument.tp.TouchPointDescriptor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Common method used by most of {@link AbstractCodeProvider} implementations.  
 * The methods realized here are independent on counters storing structure. 
 *   
 * 
 * @author piotr.tabor@gmail.com
 */
public abstract class AbstractCodeProvider implements CodeProvider {

	/**
	 * CounterId used to store unnecessary events to avoid fake jump counting in
	 * instrumented(generated) code
	 */
	public static final int FAKE_COUNTER_ID = 0;
	
	public AbstractCodeProvider() {
		super();
	}
	
	public void generateCodeThatSetsJumpCounterIdVariable(
			MethodVisitor nextMethodVisitor, int new_value,
			int lastJumpIdVariableIndex) {
		nextMethodVisitor.visitLdcInsn(new_value);
		nextMethodVisitor.visitVarInsn(Opcodes.ISTORE, lastJumpIdVariableIndex);
	}

	public void generateCodeThatZeroJumpCounterIdVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex) {
		generateCodeThatSetsJumpCounterIdVariable(nextMethodVisitor,
				FAKE_COUNTER_ID, lastJumpIdVariableIndex);
	}
	
	public void generateCodeThatIncrementsCoberturaCounterIfVariableEqualsAndCleanVariable(
			MethodVisitor nextMethodVisitor,
			Integer neededJumpCounterIdVariableValue,
			Integer counterIdToIncrement, int lastJumpIdVariableIndex,
			String className) {

		nextMethodVisitor.visitLdcInsn((int) neededJumpCounterIdVariableValue);
		nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
		Label afterJump = new Label();
		nextMethodVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, afterJump);
		generateCodeThatIncrementsCoberturaCounter(nextMethodVisitor,
				counterIdToIncrement, className);
		generateCodeThatZeroJumpCounterIdVariable(nextMethodVisitor,
				lastJumpIdVariableIndex);
		nextMethodVisitor.visitLabel(afterJump);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * The code injected by this implementation just registers the class using {@link TouchCollector#registerClass(Class)}. This way, during the  
	 * execution, touch collector knows that is responsible to ask the class after execution about a current status of the counters.  
	 */
	public void generateCINITmethod(MethodVisitor mv, String className,int countersCnt) {
		mv.visitLdcInsn(Type.getObjectType(className));
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(TouchCollector.class), "registerClass","(Ljava/lang/Class;)V");		
	}	
	
	/** 
	 * {@inheritDoc}<br/><br/>
	 * 
	 * Generates method (named  {@link #COBERTURA_CLASSMAP_METHOD_NAME}) with such a signature:
	 *  __cobertura_classmap( {@link LightClassmapListener} listener).</br>  
	 *  
	 *  The method informs the listener about all lines, jumps and switches found, and about all counters tracking 
	 *  the constructions.   
	 */
	public void generateCoberturaClassMapMethod(ClassVisitor cv,ClassMap classMap){
		final String CLASSMAP_LISTENER_INTERNALNAME=Type.getInternalName(LightClassmapListener.class);
		
		MethodVisitor mv=cv.visitMethod(
				Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, 
				COBERTURA_CLASSMAP_METHOD_NAME, 
				"("+Type.getType(LightClassmapListener.class).toString()+")V",
				null,null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD,0);
		
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn(Type.getObjectType(classMap.getClassName()));
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,CLASSMAP_LISTENER_INTERNALNAME, "setClazz", "(Ljava/lang/Class;)V");
		
		if(classMap.getSource()!=null){
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(classMap.getSource());
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,CLASSMAP_LISTENER_INTERNALNAME, "setSource", "(Ljava/lang/String;)V");			
		}
		
		List<TouchPointDescriptor> touchPointDescriptors=classMap.getTouchPointsInLineOrder();
		for (TouchPointDescriptor tpd:touchPointDescriptors){
			mv.visitInsn(Opcodes.DUP);
			mv.visitLdcInsn(tpd.getLineNumber());			
			if (tpd instanceof LineTouchPointDescriptor){
				mv.visitLdcInsn(((LineTouchPointDescriptor) tpd).getCounterId());
				mv.visitLdcInsn(((LineTouchPointDescriptor) tpd).getMethodName());
				mv.visitLdcInsn(((LineTouchPointDescriptor) tpd).getMethodSignature());
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, CLASSMAP_LISTENER_INTERNALNAME,"putLineTouchPoint","(IILjava/lang/String;Ljava/lang/String;)V");
			}else if (tpd instanceof JumpTouchPointDescriptor){
				mv.visitLdcInsn(((JumpTouchPointDescriptor) tpd).getCounterIdForTrue());
				mv.visitLdcInsn(((JumpTouchPointDescriptor) tpd).getCounterIdForFalse());
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, CLASSMAP_LISTENER_INTERNALNAME,"putJumpTouchPoint","(III)V");
			}else if (tpd instanceof SwitchTouchPointDescriptor){
				SwitchTouchPointDescriptor stpd=(SwitchTouchPointDescriptor)tpd;			
				Collection<Integer> ci=stpd.getCountersForLabels();
				mv.visitLdcInsn(ci.size());//Size of a new table
				mv.visitIntInsn(Opcodes.NEWARRAY,Opcodes.T_INT);				
				int i=0; 
				for(Integer counterId:ci){
					mv.visitInsn(Opcodes.DUP); //First for addition of items, second ad putSwitchTouchPoint parameter (or next loop iteration)
					mv.visitLdcInsn(i);
					mv.visitLdcInsn(counterId);
					mv.visitInsn(Opcodes.IASTORE);
					i++;
				}
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, CLASSMAP_LISTENER_INTERNALNAME,"putSwitchTouchPoint","(I[I)V");				
			}
		}		
		mv.visitInsn(Opcodes.POP);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);//will be recalculated by writer
		mv.visitEnd();		
	}
		

}