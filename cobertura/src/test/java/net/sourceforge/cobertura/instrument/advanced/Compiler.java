package net.sourceforge.cobertura.instrument.advanced;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class Compiler implements FileVisitor {
	private final File outDir;

	private final File srcBaseDir;
	private final List<File> srcFiles = new LinkedList<File>();

	public Compiler(File outDir, File srcBaseDir) {
		this.outDir = outDir;
		this.srcBaseDir = srcBaseDir;
		
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
				cmd[3+i] = rewriteToRelativePath(src);
				i++;
			}
			
			Process p = Runtime.getRuntime()
					.exec(
							cmd,
							new String[]{},
							srcBaseDir);
			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String line;
				while(null != (line = br.readLine())) {
					System.out.println(line);					
				}
			} finally {
				if(null != br) {
					br.close();
				}
			}
			
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

	private String rewriteToRelativePath(File src) {
		String absPath = src.getAbsolutePath();
		if(!absPath.startsWith(srcBaseDir.getAbsolutePath())) {
			throw new IllegalStateException("All sources need to be in srcBaseDir. "+absPath);
		}
		return absPath.substring(srcBaseDir.getAbsolutePath().length()+1);
	}
}
