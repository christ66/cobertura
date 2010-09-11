/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 Jiri Mares
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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * <p>Instruments given method adding a new 'int' variables that are available from the start of the method
 * to the end.</p>
 * 
 * <p>The variables have allocated that first index after method arguments. Other variables are shifted.</p>
 * 
 * <p>You can use static {@link #calculateFirstStackVariable(int, String)} to check what will be the index of 
 * first introduced variable if the adaptor would have been used.</p> 
 * 
 * <p>Expects that the visitMaxs will be calculated after that instrumentation, so ensure
 * that you call {@link ClassWriter} with {@link ClassWriter#COMPUTE_FRAMES} and {@link ClassWriter#COMPUTE_MAXS}.</p> 
 */
public class ShiftVariableMethodAdapter extends MethodAdapter implements Opcodes
{
	protected int firstStackVariable;
	protected int addedStackWords;
	
	private Label startLabel,endLabel;

	public ShiftVariableMethodAdapter(MethodVisitor mv, int access, String desc, int addedStackWords)
	{
		super(mv);
		this.firstStackVariable=calculateFirstStackVariable(access, desc);
		this.addedStackWords = addedStackWords;
	}
	
	/**
	 * Calculates index of first variable that would be added by the instrumenter. 
	 * 
	 * 
	 * @param access
	 * @param desc
	 * @return
	 */
	public static int calculateFirstStackVariable(int access, String desc){
		int firstStackVariable;
		Type[] args = Type.getArgumentTypes(desc);
		/*Zero-index variable is occupated by 'this' in case of not static method*/
		firstStackVariable = ((ACC_STATIC & access) != 0) ? 0 : 1;
		for (int i = 0; i < args.length; i++) { //number of arguments.
			firstStackVariable += args[i].getSize();
		}
		return firstStackVariable;
	}

	public void visitVarInsn(int opcode, int var) 
	{
		mv.visitVarInsn(opcode, (var >= firstStackVariable) ? var + addedStackWords : var);
	}

	public void visitIincInsn(int var, int increment) {
		mv.visitIincInsn((var >= firstStackVariable) ? var + addedStackWords : var, increment);
	}

	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
		mv.visitLocalVariable(name, desc, signature, start, end, (index >= firstStackVariable) ? index + addedStackWords : index);
	}

	public int getAddedStackWords()
	{
		return addedStackWords;
	}

	public int getFirstStackVariable()
	{
		return firstStackVariable;
	}
	
	@Override
	public void visitLabel(Label arg0) {
		if(startLabel==null){
			startLabel=arg0;
		}
		endLabel=arg0;
		super.visitLabel(arg0);
	}
	
	
	@Override
	public void visitMaxs(int arg0, int arg1) {
		if (startLabel!=null && endLabel!=null){
			for(int i=0; i<firstStackVariable; i++){
				mv.visitLocalVariable("__cobertura__internal_var_"+i, "I", null, startLabel, endLabel, firstStackVariable);
			}
		}
		super.visitMaxs(arg0, arg1);
	}

}
