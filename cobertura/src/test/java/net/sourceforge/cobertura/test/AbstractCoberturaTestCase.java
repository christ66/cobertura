/**
 * 
 */
package net.sourceforge.cobertura.test;

import groovy.util.AntBuilder;
import groovy.util.Node;
import net.sourceforge.cobertura.ant.InstrumentTask;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.reporting.Main;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Path.PathElement;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author schristou88
 *
 */
public class AbstractCoberturaTestCase {
	public static AntBuilder ant = TestUtils.antBuilder;
	public static File tempDir;
	public static File srcDir;
	public static File reportDir;
	public static File instrumentDir;
	File mainSourceFile;
	public static File datafile;

	@Before
	public void setUp() throws Exception {
		tempDir = TestUtils.getTempDir();

		FileUtils.deleteDirectory(tempDir);

		srcDir = new File(tempDir, "src");
		reportDir = new File(tempDir, "report");
		instrumentDir = new File(tempDir, "instrument");
		mainSourceFile = new File(srcDir, "mypackage/Main.java");
		datafile = new File(srcDir, "cobertura.ser");

		srcDir.mkdirs();
		reportDir.mkdirs();
		instrumentDir.mkdirs();
	}

	@After
	public void tearDown() {
		// Default is do nothing since if we try
		// debugging we can see logs of current failure.
	}

	public static void assertConditionCoverage(List<Node> lines,
			String expectedValue, int lineNumber) {
		boolean found = false;
		for (Node node : lines) {
			if (Integer.valueOf((String) node.attribute("number")) == lineNumber) {
				found = true;
				assertEquals(expectedValue, (String) node
						.attribute("condition-coverage"));
			}
		}
		assertTrue(found);
	}

	public Node createAndExecuteMainMethod(String packageName, String fileName,
			String fileContent, String mainMethod) throws Exception {
		return createAndExecuteMainMethod(packageName, fileName, fileContent,
				mainMethod, "xml");
	}

	public Node createAndExecuteMainMethod(String packageName, String fileName,
			String fileContent, String mainMethod, String format)
			throws Exception {

		FileUtils.write(new File(srcDir, fileName + ".java"), fileContent);

		TestUtils.compileSource(TestUtils.antBuilder, srcDir);

		TestUtils.instrumentClasses(TestUtils.antBuilder, srcDir, datafile,
				instrumentDir);

		/*
		 * Kick off the Main (instrumented) class.
		 */
		Java java = new Java();
		java.setProject(TestUtils.project);
		java.setClassname(mainMethod);
		java.setDir(srcDir);
		java.setFork(true);
		java.setFailonerror(true);
		java.setClasspath(TestUtils.getCoberturaDefaultClasspath());
		java.execute();
		/*
		 * Now create a cobertura xml file and make sure the correct counts are in it.
		 */
		new File(reportDir, "/coverage-xml").mkdirs();
		new File(reportDir, "/coverage-html").mkdirs();
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(reportDir, "/coverage-xml"));
		reportTask.execute();

		reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("html");
		reportTask.setDestDir(new File(reportDir, "/coverage-html"));
		reportTask.execute();

		return TestUtils.getXMLReportDOM(reportDir.getAbsolutePath()
				+ "/coverage-xml/coverage.xml");
	}

	/**
	 * 
	 * @param method method with the parsing issue
	 * @throws IOException 
	 */
	public static void parseIssueTester(String imports, String method)
			throws IOException {
		String wrapper = "\n package mypackage;" + "\n " + imports
				+ "\n public class FooMain {" + method + "\n }";

		FileUtils.write(new File(srcDir, "mypackage/FooMain.java"), wrapper);

		TestUtils.compileSource(TestUtils.antBuilder, srcDir);

		TestUtils.instrumentClasses(TestUtils.antBuilder, srcDir, datafile,
				instrumentDir);

		debugReportTask();

		if (FileUtils
				.readFileToString(new File(reportDir, "error.log"))
				.contains(
						"net.sourceforge.cobertura.javancss.parser.ParseException"))
			fail("JavaNCSS Error, see console output or file: "
					+ new File(reportDir, "error.log").getAbsolutePath());

		if (FileUtils
				.readFileToString(new File(reportDir, "std.log"))
				.contains(
						"net.sourceforge.cobertura.javancss.parser.ParseException"))
			fail("JavaNCSS Error, see console output or file: "
					+ new File(reportDir, "std.log").getAbsolutePath());
	}

	/**
	 *
	 * This report task allows you to insert break points in JavaParser class for
	 * debugging parsing issues further.
	 * 
	 * Standard output gets put in ${reportDir}/std.log
	 * Standard error  gets put in ${reportDir}/error.log
	 * 
	 * @throws FileNotFoundException
	 */
	public static void debugReportTask() throws FileNotFoundException {
		String[] args = {"--format", "xml", "--destination",
				reportDir.getAbsolutePath(), "--datafile",
				datafile.getAbsolutePath(), srcDir.getAbsolutePath()};
		PrintStream err = new PrintStream(new File(reportDir, "error.log"));
		PrintStream out = new PrintStream(new File(reportDir, "std.log"));

		PrintStream dErr = System.err;
		PrintStream dOut = System.out;

		try {
			System.setErr(err);
			System.setOut(out);
			Main.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.setErr(dErr);
			System.setOut(dOut);
		}
	}

	public static void instrumentClass(boolean threadsafeRigorous,
			boolean ignoretrivial, List<String> ignoreAnnotationNames,
			String excludeClassesRegexList, boolean individualTest) {
		FileSet fileSet = new FileSet();
		fileSet.setDir(srcDir);
		fileSet.setIncludes("**/*.class");

		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(TestUtils.project);
		instrumentTask.addFileset(fileSet);
		instrumentTask.createIncludeClasses().setRegex("mypackage.*");
		instrumentTask.setDataFile(datafile.getAbsolutePath());
		instrumentTask.setToDir(instrumentDir);
		instrumentTask.setThreadsafeRigorous(threadsafeRigorous);
		instrumentTask.setIgnoreTrivial(ignoretrivial);
		instrumentTask.createExcludeClasses().setRegex(
				(excludeClassesRegexList == null)
						? ""
						: excludeClassesRegexList);
		instrumentTask.setIndividualTest(individualTest);

		if (ignoreAnnotationNames != null) {
			for (String annotation : ignoreAnnotationNames) {
				instrumentTask.createIgnoreMethodAnnotation()
						.setAnnotationName(annotation);
			}
		}

		instrumentTask.execute();
	}

	public static void executeJunitTest(String testClass) throws Exception {
		Path classpath = new Path(TestUtils.project);
		PathElement instDirPathElement = classpath.new PathElement();
		PathElement buildDirPathElement = classpath.new PathElement();
		PathElement coberturaClassDirPathElement = classpath.new PathElement();
		PathElement computerClasspath = classpath.new PathElement();

		FileSet fileSet = new FileSet();

		instDirPathElement.setLocation(instrumentDir);
		buildDirPathElement.setLocation(srcDir);
		coberturaClassDirPathElement.setLocation(TestUtils
				.getCoberturaClassDir());
		computerClasspath.setPath(System.getProperty("java.class.path"));

		fileSet.setDir(new File("src/test/resources/antLibrary/common/groovy"));
		fileSet.setIncludes("*.jar");

		classpath.add(instDirPathElement);
		classpath.add(buildDirPathElement);
		classpath.add(coberturaClassDirPathElement);
		classpath.add(computerClasspath);
		classpath.addFileset(fileSet);

		// Create junitTask
		JUnitTask junit = new JUnitTask();
		junit.setProject(TestUtils.project);
		junit.setHaltonfailure(true);
		junit.setDir(srcDir);
		junit.setFork(true);

		// Add formatter to junitTask
		FormatterElement formatter = new FormatterElement();
		TypeAttribute type = new TypeAttribute();
		type.setValue("xml");
		formatter.setType(type);
		junit.addFormatter(formatter);

		// Add test to junitTask
		JUnitTest test = new JUnitTest(testClass);
		test.setTodir(reportDir);
		junit.addTest(test);

		junit.setShowOutput(true);

		// Add classpath to junitTask
		junit.createClasspath().add(classpath);
		System.out.println(classpath);

		// Finally execute junitTask
		junit.execute();
	}

	public static void instrumentClassIndividualTests(
			boolean threadsafeRigorous, boolean ignoretrivial,
			List<String> ignoreAnnotationNames, String excludeClassesRegexList,
			String[] testUnits) {
		FileSet fileSet = new FileSet();
		fileSet.setDir(srcDir);
		fileSet.setIncludes("**/*.class");

		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(TestUtils.project);
		instrumentTask.addFileset(fileSet);
		instrumentTask.createIncludeClasses().setRegex("mypackage.*");
		instrumentTask.setDataFile(datafile.getAbsolutePath());
		instrumentTask.setToDir(instrumentDir);
		instrumentTask.setThreadsafeRigorous(threadsafeRigorous);
		instrumentTask.setIgnoreTrivial(ignoretrivial);
		instrumentTask.createExcludeClasses().setRegex(
				(excludeClassesRegexList == null)
						? ""
						: excludeClassesRegexList);
		instrumentTask.setIndividualTest(true);

		for (String testUnit : testUnits) {
			Path p = new Path(TestUtils.project, testUnit);
			instrumentTask.createTestUnitClasses().add(p);
		}

		if (ignoreAnnotationNames != null) {
			for (String annotation : ignoreAnnotationNames) {
				instrumentTask.createIgnoreMethodAnnotation()
						.setAnnotationName(annotation);
			}
		}

		instrumentTask.execute();
	}
}
