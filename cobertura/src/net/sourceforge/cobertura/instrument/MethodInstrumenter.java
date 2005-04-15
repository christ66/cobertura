/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
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

package net.sourceforge.cobertura.instrument;

import net.sourceforge.cobertura.coveragedata.ClassData;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/*
 * TODO: If class is abstract then do not count the "public abstract class bleh" line as a SLOC.
 * TODO: For branches, only count the branch as covered if both paths are accessed?
 */
public class MethodInstrumenter extends MethodAdapter implements Opcodes
{
	private final static Perl5Matcher pm = new Perl5Matcher();
	private final String ownerClass;
	private String myName;
	private String myDescriptor;
	private Pattern ignoreRegexp;
	private ClassData classData;

	private int currentLine = 0;

	public MethodInstrumenter(ClassData classData, final MethodVisitor mv,
			final String owner, final String myName,
			final String myDescriptor, final Pattern ignoreRegexp)
	{
		super(mv);
		this.classData = classData;
		this.ownerClass = owner;
		this.myName = myName;
		this.myDescriptor = myDescriptor;
		this.ignoreRegexp = ignoreRegexp;
	}

	public void visitJumpInsn(int opcode, Label label)
	{
		super.visitJumpInsn(opcode, label);

		// Ignore any jump instructions in the "class init" method.
		// When initializing static variables, the JVM first checks
		// that the variable is null before attempting to set it.
		// This check contains an IFNONNULL jump instruction which
		// would confuse people if it showed up in the reports.
		if ((opcode != GOTO) && (currentLine != 0)
				&& (!this.myName.equals("<clinit>")))
			classData.markLineAsBranch(currentLine);
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
	{
		super.visitLookupSwitchInsn(dflt, keys, labels);
		if (currentLine != 0)
			classData.markLineAsBranch(currentLine);
	}

	public void visitLineNumber(int line, Label start)
	{
		// Record initial information about this line of code
		currentLine = line;
		classData.addLine(currentLine, myName, myDescriptor);

		// Get an instance of ProjectData:
		// ProjectData.getGlobalProjectData()
		mv.visitMethodInsn(INVOKESTATIC,
				"net/sourceforge/cobertura/coveragedata/ProjectData",
				"getGlobalProjectData",
				"()Lnet/sourceforge/cobertura/coveragedata/ProjectData;");

		// Get the ClassData object for this class:
		// projectData.getClassData("name.of.this.class")
		mv.visitLdcInsn(ownerClass);
		mv
				.visitMethodInsn(INVOKEVIRTUAL,
						"net/sourceforge/cobertura/coveragedata/ProjectData",
						"getOrCreateClassData",
						"(Ljava/lang/String;)Lnet/sourceforge/cobertura/coveragedata/ClassData;");

		// Mark the current line number as covered:
		// classData.touch(line)
		mv.visitIntInsn(SIPUSH, line);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"net/sourceforge/cobertura/coveragedata/ClassData", "touch",
				"(I)V");

		super.visitLineNumber(line, start);
	}

	public void visitMethodInsn(int opcode, String owner, String name,
			String desc)
	{
		super.visitMethodInsn(opcode, owner, name, desc);

		if ((ignoreRegexp != null) && (pm.matches(owner, ignoreRegexp)))
			classData.removeLine(currentLine);
	}

}
