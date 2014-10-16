package net.sourceforge.cobertura.instrument.advanced;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Compiler implements FileVisitor {
	private final File outDir;
	
	private final List<File> srcFiles = new LinkedList<File>();

	public Compiler(File outDir) {
		this.outDir = outDir;
		
		if(!outDir.exists()) {
			if(!outDir.mkdirs()) {
				throw new IllegalStateException("Could not create directory "+outDir);
			}
		}
	}

	public boolean accept(File f) {
		return f.getName().endsWith(".java");
	}

	public void visitFile(File f) {
		srcFiles.add(f);
	}

	public void compile() {
		try {
			String[] cmd = new String[3+srcFiles.size()];
			cmd[0] = "javac";
			cmd[1] = "-d";
			cmd[2] = outDir.getAbsolutePath();
			
			int i=0;
			for(File src:srcFiles) {
				cmd[3+i] = src.getAbsolutePath();
				i++;
			}
			
			Process p = Runtime.getRuntime()
					.exec(
							cmd,
							new String[]{},
							outDir);
			int ev = p.waitFor();
			if(0!=ev) {
				throw new IllegalStateException("Could not compile.");
			}
		} catch(IOException ex) {
			throw new IllegalStateException("Could not compile.", ex);
		} catch(InterruptedException ex) {
			throw new IllegalStateException("Interrupted while compiling.", ex);
		}
		
	}
}
