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
 * Expects that the visitMaxs is calculated for me .... 
 */
public class NewLocalVariableMethodAdapter extends MethodAdapter implements Opcodes
{
	protected int firstStackVariable;
	protected int addedStackWords;

	public NewLocalVariableMethodAdapter(MethodVisitor mv, int access, String desc, int addedStackWords)
	{
		super(mv);
		Type[] args = Type.getArgumentTypes(desc);
		firstStackVariable = ((ACC_STATIC & access) != 0) ? 0 : 1;
		for (int i = 0; i < args.length; i++) {
			firstStackVariable += args[i].getSize();
		}
		this.addedStackWords = addedStackWords;
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

}
