
package net.sourceforge.cobertura.coverage;

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

	private final String ownerClass;
	private String myName;
	private CoverageData coverageData;

	private int currentLine = 0;

	public MethodInstrumenter(final MethodVisitor mv,
			CoverageData coverageData, final String owner, final String myName)
	{
		super(mv);
		this.coverageData = coverageData;
		this.ownerClass = owner;
		this.myName = myName;
	}

	public void visitJumpInsn(int opcode, Label label)
	{
		super.visitJumpInsn(opcode, label);

		// Ignore any jump instructions in the "class init" method.
		// When initializing static variables, the JVM first checks
		// that the variable is null before attempting to set it.
		// This IFNONNULL check would confuse people if it showed
		// up in the reports.
		if ((opcode != GOTO) && (currentLine != 0)
				&& (!this.myName.equals("<clinit>")))
			coverageData.markLineAsConditional(currentLine);
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
	{
		super.visitLookupSwitchInsn(dflt, keys, labels);
		if (currentLine != 0)
			coverageData.markLineAsConditional(currentLine);
	}

	public void visitLineNumber(int line, Label start)
	{
		// Record initial information about this line of code
		currentLine = line;
		coverageData.addLine(currentLine, myName);

		// Get an instance of CoverageDataFactory
		mv.visitMethodInsn(INVOKESTATIC,
				"net/sourceforge/cobertura/coverage/CoverageDataFactory",
				"getInstance",
				"()Lnet/sourceforge/cobertura/coverage/CoverageDataFactory;");

		// Get the CoverageData object for this class
		mv.visitLdcInsn(ownerClass);
		mv
				.visitMethodInsn(
						INVOKEVIRTUAL,
						"net/sourceforge/cobertura/coverage/CoverageDataFactory",
						"newInstrumentation",
						"(Ljava/lang/String;)Lnet/sourceforge/cobertura/coverage/CoverageData;");

		// Call "coverageData.touch(line);"
		mv.visitIntInsn(SIPUSH, line);
		mv.visitMethodInsn(INVOKEVIRTUAL,
				"net/sourceforge/cobertura/coverage/CoverageData", "touch",
				"(I)V");

		super.visitLineNumber(line, start);
	}

}