
package net.sourceforge.cobertura.coverage;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodInstrumenter extends MethodAdapter implements Opcodes
{

	private final String ownerClass;
	private String myName;
	private CoverageData coverageData;
	private boolean lineContainsAConditional = false;

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
		lineContainsAConditional = true;
	}

	public void visitLabel(Label label)
	{
		super.visitLabel(label);
		//lineContainsAConditional = true;
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
	{
		super.visitLookupSwitchInsn(dflt, keys, labels);
		//lineContainsAConditional = true;
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label[] labels)
	{
		super.visitTableSwitchInsn(min, max, dflt, labels);
		//lineContainsAConditional = true;
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type)
	{
		super.visitTryCatchBlock(start, end, handler, type);
		//lineContainsAConditional = true;
	}

	public void visitLineNumber(int line, Label start)
	{
		// Record initial information about this line of code
		coverageData.addLine(line, myName, lineContainsAConditional);
		lineContainsAConditional = false;

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