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

package net.sourceforge.cobertura.coverage;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.cobertura.util.ClassGenHelper;
import net.sourceforge.cobertura.util.InstructionHelper;
import net.sourceforge.cobertura.util.InstructionListHelper;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * This is basically the same as the normal bcel method generator, with
 * a few small changes that are used by Cobertura.  The key difference
 * is that calling the <code>addInstrumentation</code> method will add
 * the necessary Cobertura bytecode to the original bytecode, and count
 * the number of lines of source code and the number of conditionals.
 */
class InstrumentMethodGen extends MethodGen
{

	private static final Logger logger = Logger
			.getLogger(InstrumentMethodGen.class);
	private final ClassGenHelper classGenHelper;

	/**
	 * The set of "conditionals" (@see Conditional). Whenever a
	 * conditional branch is encountered it is recorded here, including
	 * the next Java source line after the conditional branch, and the
	 * Java source line of the branch target. This information is later
	 * used to calculate the branch coverage rate for this method.
	 */
	private final Set conditionals = new HashSet();

	/**
	 * The set of "valid" source lines. That is, those lines of Java
	 * source code that do not represent comments, or other syntax
	 * "fluff" (e.g., "} else {"), or those lines that have been ignored
	 * because they match the ignore regex.
	 */
	private final Set sourceLineNumbers = new HashSet();

	private final Perl5Matcher pm = new Perl5Matcher();
	private Pattern ignoreRegex = null;

	InstrumentMethodGen(Method original, ClassGen cg, String ignoreRegex)
	{
		/**
		 * Copy everything from the original method
		 */
		super(original, cg.getClassName(), cg.getConstantPool());

		this.classGenHelper = ClassGenHelper.newInstance(cg);

		Perl5Compiler pc = new Perl5Compiler();

		if (ignoreRegex != null)
		{
			/**
			 * Compile the ignore regex for later usage
			 */
			try
			{
				this.ignoreRegex = pc.compile(ignoreRegex);
			}
			catch (MalformedPatternException ex)
			{
				throw new CoverageRuntimeException(ex);
			}
		}
	}

	/**
	 * The entry point for this class. We add coverage instrumentation
	 * immediately prior to every instruction found in the line number
	 * table.
	 */
	void addInstrumentation()
	{
		if (logger.isDebugEnabled())
		{
			StringBuffer sb = new StringBuffer();
			sb.append("adding instrumentation to: ");
			sb.append(getClassName());
			sb.append('.');
			sb.append(getName());
			logger.debug(sb.toString());
		}

		/**
		 * Add instrumentation to this method.
		 */
		instrumentLines(getLineNumbers());

		/**
		 * Recalculate the maxium stack size necessary for this
		 * instrumented method.
		 */
		setMaxStack();
	}

	private void instrumentLines(LineNumberGen[] lineNumberTable)
	{
		for (int i = 0; i < lineNumberTable.length; i++)
		{
			/**
			 * Don't instrument the last the "return;" at the very end
			 * of void methods.
			 */
			if ((i == (lineNumberTable.length - 1))
					&& isVoidReturningMethod()
					&& InstructionHelper.isRetInstruction(lineNumberTable[i]))
			{
				continue;
			}

			instrumentLine(lineNumberTable[i]);
		}
	}

	/**
	 * Add coverage instrumentation to the instructions representing a
	 * line of Java source code.
	 */
	private void instrumentLine(LineNumberGen lng)
	{
		if (logger.isDebugEnabled())
		{
			StringBuffer sb = new StringBuffer();
			sb.append("adding instrumentation to: ");
			sb.append(getClassName());
			sb.append('.');
			sb.append(getName());
			sb.append(" at line: ");
			sb.append(lng.getSourceLine());
			sb.append(", position: ");
			sb.append(lng.getInstruction().getPosition());
			logger.debug(sb.toString());
		}

		if (isIgnorable(classGenHelper, lng))
		{
			return;
		}

		/**
		 * If we find a conditional branch instruction in the set of
		 * instructions that represent this line of Java source code,
		 * include them in the set of conditionals, so that we can
		 * calculate the branch coverage rate.
		 */
		handleIfInstruction(lng);

		/**
		 * Add this line of Java code to the list of "valid" source lines
		 * for this method
		 */
		addSourceLine(lng);

		/**
		 * Emit and insert the coverage instrumentation code immediately
		 * prior to the first instruction representing the Java
		 * code.
		 */
		InstructionList instructionList = emitGetInstrumentationAndTouchLine(lng);

		/**
		 * Update any targeters of the original instruction to
		 * instead target the coverage instrumentation code.
		 */
		updateTargeters(lng.getInstruction(), getInstructionList().insert(
				lng.getInstruction(), instructionList));
	}

	private void handleIfInstruction(LineNumberGen lng)
	{
		if (InstructionHelper.isIfInstruction(lng))
		{
			addIfInstruction(lng, (IfInstruction)lng.getInstruction()
					.getInstruction());
			return;
		}

		InstructionHandle handle = lng.getInstruction().getNext();

		while ((handle != null) && (!hasLineNumber(handle)))
		{
			if (InstructionHelper.isIfInstruction(handle))
			{
				addIfInstruction(lng, (IfInstruction)handle.getInstruction());
				return;
			}
			handle = handle.getNext();
		}
	}

	/**
	 * We've found a conditional branch instruction. We need to record
	 * the line number immediately after the branch, and the line number
	 * of the target of the branch. We can later determine branch
	 * coverage rates for this method.
	 *
	 * @param lng The line number containing the branch instruction.
	 * @param ifInstruction The actual <code>if</code> instruction.
	 */
	private void addIfInstruction(LineNumberGen lng,
			IfInstruction ifInstruction)
	{
		if (logger.isDebugEnabled())
		{
			StringBuffer sb = new StringBuffer();
			sb.append("if instruction at line: ");
			sb.append(lng.getSourceLine());
			sb.append(", target: ");
			sb.append(getLineNumber(ifInstruction.getTarget()));
			sb.append("(for method: ");
			sb.append(getClassName());
			sb.append('.');
			sb.append(getName());
			sb.append(')');
			logger.debug(sb.toString());
		}

		/**
		 * only add the conditional branch if the target has a line number
		 */
		int lineNumber = getLineNumber(ifInstruction.getTarget());
		if (lineNumber != 0)
		{
			conditionals.add(ConditionalFactory.newConditional(lng,
					lineNumber));
		}
	}

	/**
	 * We only record the set of "valid" source lines. That is, source
	 * lines that are not comments, or contain other syntax "fluff"
	 * (e.g., "} else {"), or any line of code that is being ignored by
	 * instrumentation ignore regex. <code>addSourceLine</code> is only
	 * called if the source line represented by <code>lng</code> is a
	 * "real" line of code.
	 */
	private void addSourceLine(LineNumberGen lng)
	{
		sourceLineNumbers.add(new Integer(lng.getSourceLine()));
	}

	/**
	 * The core instrumentation. This sequence of instructions is
	 * emitted into the instrumented class on every line of original
	 * Java code.
	 */
	private InstructionList emitGetInstrumentationAndTouchLine(
			LineNumberGen lng)
	{
		InstructionList il = new InstructionList();

		/**
		 * Obtain an instance of CoverageDataInternalFactory, via a static call
		 * to CoverageDataInternalFactory.
		 */
		il.append(classGenHelper.createInvokeStatic(
				CoverageDataInternalFactory.class, "getInstance",
				CoverageDataInternalFactory.class));

		/**
		 * Create a new instance of CoverageData (or reuse an existing
		 * instance, if one is already present in the factory), for the
		 * class that we have instrumented.
		 */
		il.append(new LDC(classGenHelper.getConstantPool().addString(
				getClassName())));
		il.append(classGenHelper.createInvokeVirtual(
				CoverageDataInternalFactory.class, "newInstrumentation",
				CoverageDataInternal.class, String.class));

		/**
		 * Update the coverage counters for this line of source code, by
		 * "touching" its instrumentation.
		 */
		il.append(InstructionListHelper.push(
				classGenHelper.getConstantPool(), lng.getSourceLine()));
		il.append(classGenHelper.createInvokeInterface(CoverageData.class,
				"touch", void.class, int.class));

		return il;
	}

	/**
	 * Inserting coverage instrumentation into a method inserts
	 * additional code into the instrumented class. When this happens we
	 * need to adjust any targeters of the original instruction so that
	 * they instead target the inserted instrumentation. The
	 * instrumentation is inserted immediately prior to
	 * <code>oldTarget</code>. Adjusting the targeters to
	 * <code>newTarget</code> (the start of where the instrumentation
	 * has been added) ensures that the instrumentation is invoked as
	 * the original code would have been.
	 */
	private void updateTargeters(InstructionHandle oldTarget,
			InstructionHandle newTarget)
	{
		if (oldTarget.hasTargeters())
		{
			updateTargeters(oldTarget, newTarget, oldTarget.getTargeters());
		}
	}

	private void updateTargeters(InstructionHandle oldTarget,
			InstructionHandle newTarget, InstructionTargeter[] targeters)
	{
		for (int i = 0; i < targeters.length; i++)
		{
			targeters[i].updateTarget(oldTarget, newTarget);
		}
	}

	private int getLineNumber(InstructionHandle handle)
	{
		LineNumberGen[] lineNumberGen = getLineNumbers();
		for (int i = 0; i < lineNumberGen.length; i++)
		{
			if (lineNumberGen[i].containsTarget(handle))
			{
				return lineNumberGen[i].getSourceLine();
			}
		}
		return 0;
	}

	private boolean hasIgnoreRegex()
	{
		return ignoreRegex != null;
	}

	private boolean hasLineNumber(InstructionHandle handle)
	{
		if (getLineNumber(handle) > 0)
			return true;
		return false;
	}

	/**
	 * We can ignore (for the purposes of instrumentation) any set of
	 * instructions which are on our ignore list. Taking the instruction
	 * handle of the line number, we iterate over the instructions until
	 * we meet the next instruction that has a line number. If we
	 * encounter an instruction on our ignore list, then we can ignore
	 * (for the purposes of instrumentation) this group of instructions.
	 */
	private boolean isIgnorable(ClassGenHelper helper, LineNumberGen lng)
	{
		if (logger.isDebugEnabled())
		{
			StringBuffer sb = new StringBuffer();
			sb.append("instruction offset: ");
			sb.append(lng.getInstruction().getPosition());
			sb.append(", source line: ");
			sb.append(lng.getSourceLine());
			logger.debug(sb.toString());
		}

		if (isIgnorable(helper, lng.getInstruction()))
		{
			return true;
		}

		InstructionHandle handle = lng.getInstruction().getNext();

		/**
		 * Check any other instructions that are on this line.  Stop if
		 * if we run out of instructions or if we reach the next line.
		 */
		while ((handle != null) && (!hasLineNumber(handle)))
		{
			if (isIgnorable(helper, handle))
			{
				return true;
			}

			handle = handle.getNext();
		}

		return false;
	}

	/**
	 * We currently only ignore the following instructions:
	 * <ul>
	 * <li>
	 *   If the class is abstract, ignore anything in the
	 *   &gt;init&lt; block.
	 * </li>
	 * <li>Those matching the regular expression</li>
	 * </ul>
	 */
	private boolean isIgnorable(ClassGenHelper helper,
			InstructionHandle handle)
	{
		if (classGenHelper.getClassGen().isAbstract()
				&& getName().equals("<init>"))
		{
			return true;
		}
		else if (InstructionHelper.isInvokeInstruction(handle)
				&& hasIgnoreRegex())
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("class name: " + helper.getClassName(handle));
			}
			if (pm.matches(helper.getClassName(handle), ignoreRegex))
				return true;
		}

		return false;
	}

	private boolean isVoidReturningMethod()
	{
		return getReturnType().equals(Type.VOID);
	}

	/**
	 * @return The set of valid source line numbers.  That is, those that
	 * are not comments, nor syntax "fluff" (e.g., "} else {"), nor
	 * lines that are being ignored by the instrumentation ignore regex.
	 */
	Set getSourceLineNumbers()
	{
		return sourceLineNumbers;
	}

	/**
	 * This method is used internally to calculate the branch coverage
	 * rate for this method.
	 * @return The set of conditional branches within this method.
	 */
	Set getConditionals()
	{
		if (logger.isDebugEnabled())
		{
			StringBuffer sb = new StringBuffer();
			sb.append(getClassName());
			sb.append('.');
			sb.append(getName());
			sb.append(" conditionals: ");
			sb.append(conditionals.toString());
			logger.debug(sb.toString());
		}

		return conditionals;
	}

	String getMethodNameAndSignature()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getName());
		sb.append(getSignature());
		return sb.toString();
	}

}