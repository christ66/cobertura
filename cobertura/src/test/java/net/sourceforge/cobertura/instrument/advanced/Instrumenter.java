package net.sourceforge.cobertura.instrument.advanced;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.instrument.InstrumentMain;

public class Instrumenter implements FileVisitor {
	private final ArgumentsBuilder args = new ArgumentsBuilder();

	private final File baseDir;
	
	public Instrumenter(File destDir, File baseDir) {
		this.baseDir = baseDir;
		
		args.failOnError(true);
		args.setDataFile(new File(destDir, "cobertura.ser").getAbsolutePath());
		args.setDestinationDirectory(destDir.getAbsolutePath());
		args.setBaseDirectory(baseDir.getAbsolutePath());
	}

	public boolean accept(File f) {
		return f.getName().endsWith(".class");
	}

	public void visitFile(File f) {
		args.addFileToInstrument(f.getAbsolutePath());
	}

	public void doIt() throws Throwable {
		InstrumentMain.urlClassLoader = new URLClassLoader(new URL[] {
				baseDir.toURI().toURL()
		});
		
		Thread.currentThread().setContextClassLoader(InstrumentMain.urlClassLoader);
		
		Cobertura cobertura = new Cobertura(args.build());
		cobertura.instrumentCode();
	}
}
