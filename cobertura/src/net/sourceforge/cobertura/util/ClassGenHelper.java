/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
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

package net.sourceforge.cobertura.util;

import java.lang.reflect.Method;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;

public class ClassGenHelper
{

	final ClassGen cg;
	final InstructionFactory factory;

	private ClassGenHelper(ClassGen cg)
	{
		this.cg = cg;
		this.factory = new InstructionFactory(cg);
	}

	public static ClassGenHelper newInstance(ClassGen cg)
	{
		return new ClassGenHelper(cg);
	}

	public ClassGen getClassGen()
	{
		return cg;
	}

	public ConstantPoolGen getConstantPool()
	{
		return cg.getConstantPool();
	}

	private InstructionList createInvoke(Class cl, String methodName,
			Class returnType, Type[] parameterTypes, short kind)
	{
		InstructionList il = new InstructionList();
		il.append(factory.createInvoke(cl.getName(), methodName, TypeHelper
				.getType(returnType), parameterTypes, kind));
		return il;
	}

	public InstructionList createInvokeVirtual(Class cl, String methodName,
			Class returnType)
	{
		return createInvoke(cl, methodName, returnType, Type.NO_ARGS,
				Constants.INVOKEVIRTUAL);
	}

	public InstructionList createInvokeVirtual(Class cl, String methodName,
			Class returnType, Class signature)
	{
		return createInvokeVirtual(cl, methodName, returnType,
				new Class[] { signature });
	}

	public InstructionList createInvokeVirtual(Class cl, String methodName,
			Class returnType, Class[] signature)
	{
		return createInvoke(cl, methodName, returnType, TypeHelper
				.getTypes(signature), Constants.INVOKEVIRTUAL);
	}

	public InstructionList createInvokeStatic(Class cl, String methodName,
			Class returnType)
	{
		return createInvoke(cl, methodName, returnType, Type.NO_ARGS,
				Constants.INVOKESTATIC);
	}

	public InstructionList createInvokeStatic(Class cl, String methodName,
			Class returnType, Class[] signature)
	{
		return createInvoke(cl, methodName, returnType, TypeHelper
				.getTypes(signature), Constants.INVOKESTATIC);
	}

	public InstructionList createInvokeInterface(Class cl, String methodName,
			Class returnType)
	{
		return createInvoke(cl, methodName, returnType, Type.NO_ARGS,
				Constants.INVOKEINTERFACE);
	}

	public InstructionList createInvokeInterface(Class cl, String methodName,
			Class returnType, Class signature)
	{
		return createInvokeInterface(cl, methodName, returnType,
				new Class[] { signature });
	}

	public InstructionList createInvokeInterface(Class cl, String methodName,
			Class returnType, Class[] signature)
	{
		return createInvoke(cl, methodName, returnType, TypeHelper
				.getTypes(signature), Constants.INVOKEINTERFACE);
	}

	public InstructionList createInvokeSpecial(Class cl, String methodName,
			Class returnType)
	{
		return createInvoke(cl, methodName, returnType, Type.NO_ARGS,
				Constants.INVOKESPECIAL);
	}

	public InstructionList createInvokeSpecial(Class cl, String methodName,
			Class returnType, Class[] signature)
	{
		return createInvoke(cl, methodName, returnType, TypeHelper
				.getTypes(signature), Constants.INVOKESPECIAL);
	}

	public InstructionList createInvokeSpecial(Method m)
	{
		return createInvokeSpecial(m.getDeclaringClass(), m.getName(), m
				.getReturnType(), m.getParameterTypes());
	}

	public String getClassName(InstructionHandle handleToInvokeInstruction)
	{
		return getClassName((InvokeInstruction)handleToInvokeInstruction
				.getInstruction());
	}

	public String getClassName(InvokeInstruction invoke)
	{
		return invoke.getClassName(getConstantPool());
	}
}