package net.sourceforge.cobertura.instrument;

import java.io.InputStream;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

public class TestUnitVisitor extends ClassVisitor implements Opcodes {
	private String className;

	public TestUnitVisitor(ClassVisitor cv, InputStream is) {
		super(Opcodes.ASM4, new CheckClassAdapter(cv, false));
		
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		this.className = name;
	}

	@Override
	public MethodVisitor visitMethod(int access, String methodName,
			String description, String signature, String[] exceptions) {
		MethodVisitor nestedVisitor = super.visitMethod(access, methodName,
				description, signature, exceptions);
		nestedVisitor.visitCode();
		nestedVisitor.visitLdcInsn(className + " - " + methodName);
		nestedVisitor.visitFieldInsn(PUTSTATIC, "net/sourceforge/cobertura/Cobertura", "TestClassAndMethodNamesMerged", "Ljava/lang/String;");
		return nestedVisitor;
	}
}
