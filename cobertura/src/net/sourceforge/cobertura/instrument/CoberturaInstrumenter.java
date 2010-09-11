/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.instrument.detdup.DetectDuplicatedCodeClassVisitor;
import net.sourceforge.cobertura.util.IOUtil;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Class that is responsible for the whole process of instrumentation of a single class 
 * 
 * @author piotr.tabor@gmail.com
 */
public class CoberturaInstrumenter {
	private static final Logger logger = Logger.getLogger(CoberturaInstrumenter.class);
	
	/**
	 * During the instrumentation process we are feeling {@link ProjectData}, to generate from
	 * it the *.ser file. 
	 * 
	 * We now (1.10+) don't need to generate the file (it is not necessery for reporting), but we still
	 * do it for backward compatibility (for example maven-cobertura-plugin expects it). We should avoid
	 * this some day.
	 */
	private ProjectData projectData;
	
	/**
	 * The root directory for instrumented classes. If it is null, the instrumented classes are overwritten.  
	 */
	private File destinationDirectory;
	
	/**
	 * List of patterns to know that we don't want trace lines that are calls to some methods
	 */
	private Collection<Pattern> ignoreRegexes = new Vector<Pattern>();
	
	
	/**
	 * Analyzes and instruments class given by path. 
	 * 
	 * <p>Also the {@link #projectData} structure is filled with information about the found touch-points</p>
	 * 
	 * @param file - path to class that should be instrumented
	 * 
	 * @return instrumentation result structure or null in case of problems
	 */
	public InstrumentationResult instrumentClass(File file){
		InputStream inputStream = null;
		try{
			logger.debug("Working on file:"+file.getAbsolutePath());
			inputStream = new FileInputStream(file);
			return instrumentClass(inputStream);
		}catch (Throwable t){
			logger.warn("Unable to instrument file " + file.getAbsolutePath(),t);
			return null;
		}finally{
			IOUtil.closeInputStream(inputStream);
		}
	}
	
	/**
	 * Analyzes and instruments class given by inputStream  
	 * 
	 * <p>Also the {@link #projectData} structure is filled with information about the found touch-points</p>
	 * 
	 * @param inputStream - source of class to instrument	 * 
	 * @return instrumentation result structure or null in case of problems
	 */
	public InstrumentationResult instrumentClass(InputStream inputStream) throws IOException{
		ClassReader cr0 = new ClassReader(inputStream);
		ClassWriter cw0 = new ClassWriter(0);
		DetectDuplicatedCodeClassVisitor cv0=new DetectDuplicatedCodeClassVisitor(cw0);
		cr0.accept(cv0,0);		
		
		
		ClassReader cr = new ClassReader(cw0.toByteArray());
		ClassWriter cw = new ClassWriter(0);
		BuildClassMapClassInstrumenter cv = new BuildClassMapClassInstrumenter(cw, ignoreRegexes,cv0.getDuplicatesLinesCollector());
			//new ClassInstrumenter(projectData, cw, ignoreRegexes, ignoreBranchesRegexes);
		cr.accept(cv, 0);
				
		if(logger.isDebugEnabled()){
			logger.debug("=============== Detected duplicated code =============");
			Map<Integer, Map<Integer, Integer>> l=cv0.getDuplicatesLinesCollector();
			for(Map.Entry<Integer, Map<Integer,Integer>> m:l.entrySet()){
				if (m.getValue()!=null){
					for(Map.Entry<Integer, Integer> pair:m.getValue().entrySet()){
						logger.debug(cv.getClassMap().getClassName()+":"+m.getKey()+" "+pair.getKey()+"->"+pair.getValue());
					}
				}
			}
			logger.debug("=============== End of detected duplicated code ======");
		}


		//XXX Don't like the idea, but we have to be compatible (hope to remove the line in future release)
		logger.debug("Puting in projectData to store in file: "+cv.getClassMap().getClassName());
		cv.getClassMap().applyOnProjectData(projectData,cv.shouldBeInstrumented());
		
		if (cv.shouldBeInstrumented()){					
			/*
			 *  BuildClassMapClassInstrumenter and DetectDuplicatedCodeClassVisitor don't modificate bytecode, 
			 *  so we can use any bytecode representation of the class
			 */
			ClassReader cr2= new ClassReader(cw0.toByteArray()); 			
			ClassWriter cw2= new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
			cv.getClassMap().assignCounterIds();
			logger.debug("Assigned "+ cv.getClassMap().getMaxCounterId()+" counters for class:"+cv.getClassMap().getClassName());
			InjectCodeClassInstrumenter cv2 = new InjectCodeClassInstrumenter(cw2, ignoreRegexes, cv.getClassMap(),cv0.getDuplicatesLinesCollector());
			cr2.accept(cv2, 0);			
			return new InstrumentationResult(cv.getClassMap().getClassName(), cw2.toByteArray());
		}else{
			logger.debug("Class shouldn't be instrumented: "+cv.getClassMap().getClassName());
			return null;
		}
	}
	
	/**
	 * Analyzes and instruments class given by file.
	 * 
	 * <p>If the {@link #destinationDirectory} is null, then the file is overwritten,
	 * otherwise the class is stored into the {@link #destinationDirectory}</p>
	 * 
	 * <p>Also the {@link #projectData} structure is filled with information about the found touch-points</p>
	 * 
	 * @param file - source of class to instrument 
	 */
	public void addInstrumentationToSingleClass(File file)
	{
		logger.debug("Instrumenting class " + file.getAbsolutePath());

		InstrumentationResult instrumentationResult=instrumentClass(file);
		if (instrumentationResult!=null){
			OutputStream outputStream = null;
			try{
				// If destinationDirectory is null, then overwrite
				// the original, uninstrumented file.
				File outputFile=(destinationDirectory == null)?file
						:new File(destinationDirectory, instrumentationResult.className.replace('.', File.separatorChar)+ ".class");
				logger.debug("Writing instrumented class into:"+outputFile.getAbsolutePath());
	
				File parentFile = outputFile.getParentFile();
				if (parentFile != null){
					parentFile.mkdirs();
				}				
	
				outputStream = new FileOutputStream(outputFile);
				outputStream.write(instrumentationResult.content);			
			}catch (Throwable t){
				logger.warn("Unable to write instrumented file " + file.getAbsolutePath(),t);
				return;
			}finally{
				outputStream = IOUtil.closeOutputStream(outputStream);
			}
		}
	}
	
// ----------------- Getters and setters -------------------------------------	
	
	/**
	 * Gets the root directory for instrumented classes. If it is null, the instrumented classes are overwritten.  
	 */
	public File getDestinationDirectory() {
		return destinationDirectory;
	}
	
	/**
	 *Sets the root directory for instrumented classes. If it is null, the instrumented classes are overwritten.  
	 */
	public void setDestinationDirectory(File destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}
	
	/**
	 * Gets list of patterns to know that we don't want trace lines that are calls to some methods
	 */
	public Collection<Pattern> getIgnoreRegexes() {
		return ignoreRegexes;
	}
	
	/**
	 * Sets list of patterns to know that we don't want trace lines that are calls to some methods
	 */
	public void setIgnoreRegexes(Collection<Pattern> ignoreRegexes) {
		this.ignoreRegexes = ignoreRegexes;
	}

	/**
	 * Sets {@link ProjectData} that will be filled with information about touch points inside instrumented classes 
	 * @param projectData
	 */
	public void setProjectData(ProjectData projectData) {
		this.projectData=projectData;		
	}

	/**
	 * Result of instrumentation is a pair of two fields: 
	 * <ul>
	 *  <li> {@link #content} - bytecode of the instrumented class
	 *  <li> {@link #className} - className of class being instrumented 
	 * </ul>
	 */
	public static class InstrumentationResult{
		private String className;
		private byte[] content;
		public InstrumentationResult(String className,byte[] content) {
			this.className=className;
			this.content=content;			
		}
		
		public String getClassName() {
			return className;
		}
		public byte[] getContent() {
			return content;
		}
	}
}
