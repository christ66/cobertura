
package net.sourceforge.cobertura.coverage;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ClassInstrumenter extends ClassAdapter implements Opcodes
{

	private final static String hasBeenInstrumented = "net/sourceforge/cobertura/coverage/HasBeenInstrumented";
	private CoverageData coverageData;
	private String myName;
	private boolean doNotInstrument = false;

	public String getClassName()
	{
		return this.myName;
	}

	public ClassInstrumenter(final ClassVisitor cv)
	{
		super(cv);
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
			doNotInstrument = true;
			super.visit(version, access, name, signature, superName,
					interfaces);
		}
		else
		{
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

		if (doNotInstrument)
			return mv;

		// TODO: Signature is always null... why?
		return mv == null ? null : new MethodInstrumenter(mv, coverageData,
				this.myName, name + "(" + signature + ")");
	}

	public void visitEnd()
	{
		if (!doNotInstrument
				&& coverageData.getValidLineNumbers().size() == 0)
			System.out
					.println("Warning: No line number information found for class "
							+ this.myName
							+ ".  Perhaps you need to compile with debug=true?");
	}
}