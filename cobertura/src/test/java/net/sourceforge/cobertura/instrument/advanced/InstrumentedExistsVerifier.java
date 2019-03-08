package net.sourceforge.cobertura.instrument.advanced;

import java.io.File;

import org.junit.Assert;

public class InstrumentedExistsVerifier implements FileVisitor {
	private final File baseDir;
	private final File instrumented;
	
	

	public InstrumentedExistsVerifier(File baseDir, File instrumented) {
		this.baseDir = baseDir;
		this.instrumented = instrumented;
	}

	public boolean accept(File f) {
		return f.getName().endsWith(".class");
	}

	public void visitFile(File f) {
		String relPath = f.getAbsolutePath().substring(baseDir.getAbsolutePath().length()+1);
		
		File needed = new File(instrumented, relPath);
		
		Assert.assertTrue("File "+needed.getAbsolutePath()+" has not been created!",
				needed.exists());
	}

}
