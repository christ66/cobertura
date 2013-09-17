package net.sourceforge.cobertura.instrument;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

public class TestUnitInstrumenter {
	public TestUnitInstrumenter(List<File> testUnitFiles) {
		LOGGER.debug("================== Start Instrumenting Test Units ==================");

		FileInputStream fis = null;
		try {
			for (File file : testUnitFiles) {
				fis = new FileInputStream(file);
				System.err.println(file.getAbsolutePath());
				ClassReader cr = new ClassReader(fis);
				
				ClassWriter cw = new CoberturaClassWriter(ClassWriter.COMPUTE_FRAMES);
				
				TestUnitVisitor cv = new TestUnitVisitor(cw, fis);
				
				// TODO: For scala we insert do a check and override a new TestUnitClassVisitor specific
				// to scala.
				
				cr.accept(cv, ClassReader.EXPAND_FRAMES);

				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), false,
						pw);
				System.err.println(sw.toString());
				
		        DataOutputStream dos=new DataOutputStream(new FileOutputStream(file));
		        dos.write(cw.toByteArray());
		        dos.flush();
		        dos.close();
				
				LOGGER.debug("Instrumented: " + file.getName());	
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			LOGGER.debug("================== Done Instrumenting Test Units ==================");
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger(TestUnitInstrumenter.class);
}
