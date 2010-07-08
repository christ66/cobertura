/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2006 Jiri Mares
 * Copyright (C) 2008 Scott Frederick
 * Copyright (C) 2010 Tad Smith 
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.util.RegexUtil;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public class FirstPassMethodInstrumenter extends MethodAdapter implements Opcodes
{

	private final String ownerClass;

	private final String ownerSuperClass;

	private String myName;

	private String myDescriptor;

	private int myAccess;
   
	private Collection ignoreRegexs;
   
	private Collection ignoreBranchesRegexs;

	private Collection ignoreMethodAnnotations;

	private boolean ignoreTrivial = false;
	private boolean ignored = false;
	private boolean mightBeTrivial = false;
	private boolean isGetter = false;
	private boolean isSetter = false;
	private boolean isInit = false;

	private ClassData classData;

	private int currentLine;
   
	private int currentJump;
   
	private int currentSwitch;
	
	private Map jumpTargetLabels;

	private Map switchTargetLabels;
   
	private Map lineLabels;
   
	private MethodVisitor writerMethodVisitor;
   
	private MethodNode methodNode;

	public FirstPassMethodInstrumenter(ClassData classData, final MethodVisitor mv,
			final String owner, final String superOwner, final int access, final String name, final String desc, 
			final String signature, final String[] exceptions, final Collection ignoreRegexs,
			final Collection ignoreBranchesRegexs, final Collection ignoreMethodAnnotations,
			final boolean ignoreTrivial)
	{
		super(new MethodNode(access, name, desc, signature, exceptions));
		this.writerMethodVisitor = mv;
		this.ownerClass = owner;
		this.ownerSuperClass = superOwner;
		this.methodNode = (MethodNode) this.mv;
		this.classData = classData;
		this.myAccess = access;
		this.myName = name;
		this.myDescriptor = desc;
		this.ignoreRegexs = ignoreRegexs;
		this.ignoreBranchesRegexs = ignoreBranchesRegexs;
		this.ignoreMethodAnnotations = ignoreMethodAnnotations;
		this.ignoreTrivial = ignoreTrivial;
		this.jumpTargetLabels = new HashMap();
		this.switchTargetLabels = new HashMap();
		this.lineLabels = new HashMap();
		this.currentLine = 0;

		if (ignoreTrivial)
		{
			checkForTrivialSignature();
		}
	}

	private void checkForTrivialSignature()
	{
		Type[] args = Type.getArgumentTypes(myDescriptor);
		Type ret = Type.getReturnType(myDescriptor);

		if (myName.equals("<init>"))
		{
			isInit = true;
			mightBeTrivial = true;
			return;
		}

		// a "setter" method must:
		// - have a name starting with "set"
		// - take one arguments
		// - return nothing (void)
		if (myName.startsWith("set") && args.length == 1 && ret.equals(Type.VOID_TYPE))
		{
			isSetter = true;
			mightBeTrivial = true;
			return;
		}

		// a "getter" method must:
		// - have a name starting with "get", "is", or "has"
		// - take no arguments
		// - return a value (non-void)
		if ((myName.startsWith("get") || myName.startsWith("is") || myName.startsWith("has")) &&
			args.length == 0 && !ret.equals(Type.VOID_TYPE))
		{
			isGetter = true;
			mightBeTrivial = true;
			return;
		}
	}

	public void visitEnd() {
		super.visitEnd();

		// if we get to the end and nothing has ruled out this method being trivial,
		// then it must be trivial, so we'll ignore it, if configured to do so
		if(ignoreTrivial && mightBeTrivial) {
			ignored = true;
		}
		
		if(ignored) {
			Iterator iter = lineLabels.values().iterator();
			while (iter.hasNext())
			{
				classData.removeLine(((Integer) iter.next()).intValue());
			}
			lineLabels.clear();
		}

		methodNode.accept(lineLabels.isEmpty() ? writerMethodVisitor : new SecondPassMethodInstrumenter(this)); //when there is no line number info -> no instrumentation
	}

	public void visitJumpInsn(int opcode, Label label)
	{
		// Ignore any jump instructions in the "class init" method.
		// When initializing static variables, the JVM first checks
		// that the variable is null before attempting to set it.
		// This check contains an IFNONNULL jump instruction which
		// would confuse people if it showed up in the reports.
		if ((opcode != GOTO) && (opcode != JSR) && (currentLine != 0)
				&& (!this.myName.equals("<clinit>")))
		{
			classData.addLineJump(currentLine, currentJump);
			jumpTargetLabels.put(label, new JumpHolder(currentLine, currentJump++));
		}
		
		markNonTrivial();
		
		super.visitJumpInsn(opcode, label);
	}

	public void visitLineNumber(int line, Label start)
	{
		// Record initial information about this line of code
		currentLine = line;
		classData.addLine(currentLine, myName, myDescriptor);
		currentJump = 0;
		currentSwitch = 0;
      
		lineLabels.put(start, new Integer(line));

		//removed because the MethodNode doesn't reproduce visitLineNumber where they are but at the end of the file :-(( 
		//therefore we don't need them
		//We can directly instrument the visit line number here, but it is better to leave all instrumentation in the second pass
		//therefore we just collects what label is the line ...
		//super.visitLineNumber(line, start);
	}

	public void visitFieldInsn(int opcode, String string, String string1, String string2)
	{
		super.visitFieldInsn(opcode, string, string1, string2);

		if (!ignored && mightBeTrivial)
		{
			// trivial opcodes for accessing class fields are:
			// - GETFIELD or PUTFIELD
			if ((isGetter && opcode != GETFIELD) ||
				(isSetter && opcode != PUTFIELD) ||
				(isInit && opcode != PUTFIELD))
			{
				markNonTrivial();
			}
		}
	}

	public void visitVarInsn(int opcode, int i1)
	{
		super.visitVarInsn(opcode, i1);

		if (!ignored && mightBeTrivial)
		{
			if (
				opcode == ILOAD ||
				opcode == LLOAD ||
				opcode == FLOAD ||
				opcode == DLOAD ||
				opcode == ALOAD
				)
			{
				// trivial opcodes for accessing local variables.
			}
			else 
			{
				markNonTrivial();
			}
		}
	}

	public void visitMethodInsn(int opcode, String owner, String name,
			String desc)
	{
		super.visitMethodInsn(opcode, owner, name, desc);

		// If any of the ignore patterns match this line
		// then remove it from our data
		if (RegexUtil.matches(ignoreRegexs, owner)) 
		{
			classData.removeLine(currentLine);
		}
		if (!ignored && mightBeTrivial)
		{
			if (isInit)
			{
				// trivial initializers can invoke parent initializers,
				// but cannot invoke any other methods
				if (opcode == INVOKESPECIAL && name.equals("<init>") && owner.equals(ownerSuperClass))
				{
					// trivial call to super constructor
				}
				else
				{
					markNonTrivial();
				}
			}
			else
			{
				markNonTrivial();
			}
		}
	}
	
	public void visitTypeInsn(int i, String string) {
		super.visitTypeInsn(i, string);
		markNonTrivial();
	}

	public void visitIntInsn(int i, int i1) {
		super.visitIntInsn(i, i1);
		markNonTrivial();
	}

	public void visitLdcInsn(Object object) {
		super.visitLdcInsn(object);
		markNonTrivial();
	}

	public void visitIincInsn(int i, int i1) {
		super.visitIincInsn(i, i1);
		markNonTrivial();
	}

	public void visitMultiANewArrayInsn(String string, int i) {
		super.visitMultiANewArrayInsn(string, i);
		markNonTrivial();
	}
	
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
	{
		super.visitLookupSwitchInsn(dflt, keys, labels);
      
		if (currentLine != 0)
		{
			switchTargetLabels.put(dflt, new SwitchHolder(currentLine, currentSwitch, -1)); 
			for (int i = labels.length -1; i >=0; i--)
				switchTargetLabels.put(labels[i], new SwitchHolder(currentLine, currentSwitch, i));
			classData.addLineSwitch(currentLine, currentSwitch++, keys);
		}
		
		markNonTrivial();
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		// We need to convert desc to a fully-qualified classname.  Example:
		//   java.lang.Override --> passed in as this: "Ljava/lang/Override;"
		if(desc.charAt(0) == 'L' && desc.charAt(desc.length() - 1) == ';')
		{
			desc = desc.substring(1, desc.length() - 1).replace('/', '.');
		}
		
		// Check to see if this annotation is one of the ones that we use to 
		// trigger us to ignore this method
		if(ignoreMethodAnnotations.contains(desc))
		{
			ignored = true;
		}
			
		return super.visitAnnotation(desc, visible);
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
	{
		super.visitTableSwitchInsn(min, max, dflt, labels);
      
		if (currentLine != 0)
		{
			switchTargetLabels.put(dflt, new SwitchHolder(currentLine, currentSwitch, -1)); 
			for (int i = labels.length -1; i >=0; i--)
				switchTargetLabels.put(labels[i], new SwitchHolder(currentLine, currentSwitch, i));
			classData.addLineSwitch(currentLine, currentSwitch++, min, max);
		}
		markNonTrivial();
	}

	protected void removeLine(int lineNumber) 
	{
		classData.removeLine(lineNumber);
	}
   
	protected MethodVisitor getWriterMethodVisitor() 
	{
		return writerMethodVisitor;
	}

	protected Collection getIgnoreRegexs() 
	{
		return ignoreRegexs;
	}

	protected Map getJumpTargetLabels() 
	{
		return jumpTargetLabels;
	}

	protected Map getSwitchTargetLabels() 
	{
		return switchTargetLabels;
	}

	protected int getMyAccess() 
	{
		return myAccess;
	}

	protected String getMyDescriptor() 
	{
		return myDescriptor;
	}

	protected String getMyName() 
	{
		return myName;
	}

	protected String getOwnerClass() 
	{
		return ownerClass;
	}

	protected Map getLineLabels() 
	{
		return lineLabels;
	}

	private void markNonTrivial()
	{
		mightBeTrivial = false;
	}


}
