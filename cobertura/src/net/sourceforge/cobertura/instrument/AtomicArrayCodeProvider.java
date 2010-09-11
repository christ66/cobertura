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

package net.sourceforge.cobertura.instrument;

import java.util.concurrent.atomic.AtomicIntegerArray;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * <p>The {@link CodeProvider} uses {@link AtomicArrayCodeProvider} to store counters.</p>
 * 
 * This implementation is totally thread-safe, but significantly slower then {@link FastArrayCodeProvider}.
 * 
 * @author piotr.tabor@gmail.com
 */
public class AtomicArrayCodeProvider extends AbstractCodeProvider implements CodeProvider {
	/**
	 * Type of the generated field, that is used to store counters 
	 */
	static final String COBERTURA_COUNTERS_FIELD_TYPE = Type.getType(AtomicIntegerArray.class).toString();
	
	public void generateCountersField(ClassVisitor cv) {
		FieldVisitor fv=cv.visitField(Opcodes.ACC_STATIC|Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL/*|Opcodes.ACC_VOLATILE*/, 
				COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE, null, null);
		fv.visitEnd();		
	}
	
	public void generateCINITmethod(MethodVisitor mv,String className,int counters_cnt){
		super.generateCINITmethod(mv,className,counters_cnt);
		mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(AtomicIntegerArray.class));
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn(counters_cnt);
	    mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
	    		Type.getInternalName(AtomicIntegerArray.class), "<init>", "(I)V");
	    mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
	    		COBERTURA_COUNTERS_FIELD_NAME,
	    		COBERTURA_COUNTERS_FIELD_TYPE);
	}
	
	public void generateCodeThatIncrementsCoberturaCounter(MethodVisitor nextMethodVisitor, Integer counterId,String className) {
		/*cobertura_counters.incrementAndGet(i);*/
		/*cobertura_counters.*/nextMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC,  className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		/*index:*/nextMethodVisitor.visitLdcInsn((int)counterId);
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Type.getInternalName(AtomicIntegerArray.class), "incrementAndGet", "(I)I");
		nextMethodVisitor.visitInsn(Opcodes.POP);
	}
	
	public void generateCodeThatIncrementsCoberturaCounterFromInternalVariable(MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex, String className) {
		/*cobertura_counters.incrementAndGet(value('lastJumpIdVariableIndex'));*/		
		/*cobertura_counters.*/nextMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, className, COBERTURA_COUNTERS_FIELD_NAME, COBERTURA_COUNTERS_FIELD_TYPE);
		/*index:*/nextMethodVisitor.visitVarInsn(Opcodes.ILOAD, lastJumpIdVariableIndex);
		nextMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,Type.getInternalName(AtomicIntegerArray.class), "incrementAndGet", "(I)I");
		nextMethodVisitor.visitInsn(Opcodes.POP);
	}	
	
}
