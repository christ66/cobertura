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

package net.sourceforge.cobertura.coverage;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ClassInstrumenter extends ClassAdapter implements Opcodes
{

	private static final Logger logger = Logger.getLogger(Main.class);

	private final static String hasBeenInstrumented = "net/sourceforge/cobertura/coverage/HasBeenInstrumented";
	private Pattern ignoreRegexp;
	private CoverageData coverageData;
	private String myName;
	private boolean instrument = false;

	public String getClassName()
	{
		return this.myName;
	}

	public boolean isInstrumented()
	{
		return instrument;
	}

	public ClassInstrumenter(final ClassVisitor cv, Pattern ignoreRegexp)
	{
		super(cv);
		this.ignoreRegexp = ignoreRegexp;
	}

	private boolean arrayContains(Object[] array, Object key)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i].equals(key))
				return true;
		}

		return false;
	}

	/**
	 * @param name In the format
	 *             "net/sourceforge/cobertura/coverage/ClassInstrumenter"
	 */
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces)
	{
		this.myName = name.replace('/', '.');
		coverageData = CoverageDataFactory.getInstance().newInstrumentation(
				this.myName);

		// Do not attempt to instrument interfaces or classes that
		// have already been instrumented
		if (((access & ACC_INTERFACE) != 0)
				|| arrayContains(interfaces, hasBeenInstrumented))
		{
			super.visit(version, access, name, signature, superName,
					interfaces);
		}
		else
		{
			instrument = true;

			// Flag this class as having been instrumented
			String[] newInterfaces = new String[interfaces.length + 1];
			System.arraycopy(interfaces, 0, newInterfaces, 0,
					interfaces.length);
			newInterfaces[newInterfaces.length - 1] = hasBeenInstrumented;

			super.visit(version, access, name, signature, superName,
					newInterfaces);
		}
	}

	public void visitSource(String source, String debug)
	{
		super.visitSource(source, debug);
		coverageData.setSourceFileName(source);
	}

	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions)
	{
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
				exceptions);

		if (!instrument)
			return mv;

		return mv == null ? null : new MethodInstrumenter(mv, coverageData,
				this.myName, name, desc, ignoreRegexp);
	}

	public void visitEnd()
	{
		if (instrument && coverageData.getValidLineNumbers().size() == 0)
			logger.warn("No line number information found for class "
					+ this.myName
					+ ".  Perhaps you need to compile with debug=true?");
	}

}
