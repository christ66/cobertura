package net.sourceforge.cobertura.instrument.advanced;

import java.io.File;
import java.util.Stack;

import org.junit.Test;

public class AdvancedCoberturaInstrumenterTest {

	@Test
	public void testInstrumentation() throws Throwable {
		File outDir = new File("target/advancedInstrumenting");

		File testSrcdir = new File("src/test/resources/advancedInstrumenting");		
		if(!testSrcdir.exists()) {
			throw new IllegalStateException("The input files for the tests are missing.");
		}
		
		File compiled = new File(outDir, "compiled");
		Compiler compiler = new Compiler(compiled, testSrcdir);
		
		File instrumented = new File(outDir, "instrumented");
		Instrumenter instrumenter = new Instrumenter(instrumented, compiled);
		
		
		// compile the files
		traverseDir(compiler, testSrcdir);
		compiler.compile();
		
		// and try to instrument them
		traverseDir(instrumenter, compiled);
	
		instrumenter.doIt();
		
		// now verify the instrumented classes are present
		// please mind that this does not ensure that the
		// instrumentation is correct
		traverseDir(new InstrumentedExistsVerifier(compiled, instrumented), compiled);
	}

	private void traverseDir(FileVisitor visitor, File testdir) {
		Stack<File> dirs = new Stack<File>();
		dirs.add(testdir);
		
		while(!dirs.isEmpty()) {
			File dir = dirs.pop();
			for(File f:dir.listFiles()) {
				if(f.isDirectory()) {
					dirs.push(f);
				} else if(visitor.accept(f)) {
					visitor.visitFile(f);
				}
			}
		}
	}
}
