package net.sourceforge.cobertura.instrument.advanced;

import java.io.File;

public interface FileVisitor {
	public boolean accept(File f);
	public void visitFile(File f);
}
