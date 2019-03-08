package net.sourceforge.cobertura.webapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import groovy.util.Node;
import net.sourceforge.cobertura.ant.InstrumentTask;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.webapp.FlushCoberturaServlet;

/**
 * This test tests the functional FlushCoberturaServlet used for Jetty servlets.
 * 
 * We use the ServletTester API to remove the sensitive tests that were in
 * the previous versions of cobertura.
 * 
 * @author schristou88
 */
public class WebAppFunctionalTest {
    
    private static final String SIMPLE_SERVLET_CLASSNAME = "com.acme.servlet.SimpleServlet";
    private static final String SAVE_DATA_METHOD_NAME = "doGet";
    
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));
    
    private ServletTester tester;
    private HttpTester request;
    private HttpTester response;
    
    private File xmlReportFile;

	@After
	public void tearDown() throws Exception {
	    
	    FileUtils.deleteQuietly(new File("cobertura.ser"));
	    
		if (tester != null) {
			tester.stop();
		}
	}

	@Test
	public void testServletCreation() throws Exception {
	    
		startUpServlet();
		pingServer();
	}

	@Test
	public void testCoberturaServlet() throws Exception {
	    
		startUpServlet();
		pingCoberturaServer();
	}

	@Test
	public void testFlushCoberturaData() throws Exception {
	    
		createSimpleWar();
		createCoberturaServlet();
		instrumentWar();
		startUpServlet();
		pingServer();
		generateReportFile();

		Node dom = TestUtils.getXMLReportDOM(xmlReportFile);

		int hitCountBefore = TestUtils.getHitCount(dom,
				SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(0, hitCountBefore);

		pingCoberturaServer();
		generateReportFile();

		dom = TestUtils.getXMLReportDOM(xmlReportFile);

		int hitCountAfter = TestUtils.getHitCount(dom,
				SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		tester.stop();

		assertEquals("Hit count should have increased by one",
				hitCountBefore + 1, hitCountAfter);
	}

	@Test
	public void testFlushCoberturaData2() throws Exception {
	    
		createSimpleWar();
		createCoberturaServlet();
		instrumentWar();
		startUpServlet();
		pingServer();
		generateReportFile();

		Node dom = TestUtils.getXMLReportDOM(xmlReportFile);

		int hitCountBefore = TestUtils.getHitCount(dom,
				SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(0, hitCountBefore);

		pingCoberturaServer();
		generateReportFile();

		dom = TestUtils.getXMLReportDOM(xmlReportFile);

		int hitCountAfter = TestUtils.getHitCount(dom,
				SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(2, hitCountAfter);

		tester.stop();

		generateReportFile();

		dom = TestUtils.getXMLReportDOM(xmlReportFile);

		int hitCountFinal = TestUtils.getHitCount(dom,
				SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(hitCountAfter, hitCountFinal);
	}

	@Test
	public void testFlushCoberturaDataOnly() throws Exception {

	    createSimpleWar();
		createCoberturaServlet();
		instrumentWar();
		startUpServlet();
		generateReportFile();

		Node dom = TestUtils.getXMLReportDOM(xmlReportFile);

		int hitCountBefore = TestUtils.getHitCount(dom,
				SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(0, hitCountBefore);

		generateReportFile();

		dom = TestUtils.getXMLReportDOM(xmlReportFile);

		int hitCountAfter = TestUtils.getHitCount(dom,
				SIMPLE_SERVLET_CLASSNAME, SAVE_DATA_METHOD_NAME);

		assertEquals(0, hitCountAfter);

		tester.stop();

		generateReportFile();

		dom = TestUtils.getXMLReportDOM(xmlReportFile);

		int finalCount = TestUtils.getHitCount(dom, SIMPLE_SERVLET_CLASSNAME,
				SAVE_DATA_METHOD_NAME);

		assertEquals(0, finalCount);
	}

	public void startUpServlet() throws Exception {
	    
		tester = new ServletTester();
		request = new HttpTester();
		response = new HttpTester();

		ClassLoader loader = createClassLoader();
		tester.setClassLoader(loader);

		tester.addServlet("com.acme.servlet.SimpleServlet", "/simple/SimpleServlet");
		tester.addServlet(FlushCoberturaServlet.class, "/coberturaFlush/flushCobertura");
		tester.start();

		request.setMethod("GET");
		request.setHeader("host", "tester");
		request.setVersion("HTTP/1.0");
	}

	public void pingServer() throws Exception {
	    
		request.setURI("/simple/SimpleServlet");
		response.parse(tester.getResponses(request.generate()));
		assertNull(response.getMethod());
		assertEquals(200, response.getStatus());
		assertEquals("Hi", response.getContent().trim());
	}

	public void pingCoberturaServer() throws Exception {
	    
		request.setURI("/coberturaFlush/flushCobertura");
		response.parse(tester.getResponses(request.generate()));
		assertNull(response.getMethod());
		assertEquals(200, response.getStatus());
		assertNull(response.getContent());
	}

	private void createSimpleWar() throws IOException {
	    
		File webappsDir = temporaryFolder.newFolder("webapps");
		File war = new File(webappsDir, "simple.war");

		File classesDir = new File("target/build/simpleWarClasses/");
		if (!classesDir.exists()) {
			classesDir.mkdirs();
		}

		FileUtils.copyDirectory(new File("target/test-classes/com"), new File(classesDir, "com"));

		War antWar = new War();
		antWar.setProject(TestUtils.project);
		antWar.setDestFile(war);
		antWar.setWebxml(new File("src/test/resources/testJetty/web.xml"));

		ZipFileSet classesFileSet = new ZipFileSet();
		classesFileSet.setDir(classesDir);

		antWar.addClasses(classesFileSet);
		antWar.execute();
	}

	private void createCoberturaServlet() throws IOException {
	    
	    File webappsDir = new File(temporaryFolder.getRoot(), "webapps");
		File war = new File(webappsDir, "coberturaFlush.war");

		File classesDir = new File("target/build/warClasses");
		if (!classesDir.exists()) {
			classesDir.mkdirs();
		}
		
		Javac javac = new Javac();
		javac.setProject(TestUtils.project);
		javac.setSrcdir(new Path(TestUtils.project, "src/main/java"));
		javac.setDestdir(classesDir);
		javac.setDebug(true);

		Path classpath = new Path(TestUtils.project);
		FileSet jettyFileSet = new FileSet();
		jettyFileSet.setDir(new File("src/test/resources/jetty"));
		jettyFileSet.setIncludes("**/*.jar");
		classpath.addFileset(jettyFileSet);

		javac.setIncludes("**/FlushCoberturaServlet.java");

		javac.setClasspath(classpath);
		javac.execute();

		War antWar = new War();
		antWar.setProject(TestUtils.project);
		antWar.setDestFile(war);
		antWar.setWebxml(new File("src/main/webapp/WEB-INF/web.xml"));

		ZipFileSet classesFileSet = new ZipFileSet();
		classesFileSet.setDir(classesDir);

		antWar.addClasses(classesFileSet);
		antWar.execute();
	}

	private ClassLoader createClassLoader() throws Exception {
	    
		File simplewar = new File(temporaryFolder.getRoot(), "webapps/simple.war");
		File coberturawar = new File(temporaryFolder.getRoot(), "webapps/coberturaFlush.war");

		@SuppressWarnings("deprecation")
		URL[] urls = new URL[]{ simplewar.toURL(), coberturawar.toURL() };

		System.out.println(Arrays.toString(urls));

		return new URLClassLoader(urls);
	}

	private void instrumentWar() {
	    
		instrumentWar(new File(temporaryFolder.getRoot(), "webapps/simple.war"));
		
		// Future: There is an issue with ServetTester api and adding a custom classloader.
		// When specify the instrumented .war file it still prefers to use the
		// target/test-classes/**/*.class files instead. In this situation we instrument the
		// classes directly but a better solution should be provided for this.
		instrumentClasses(new File("target/test-classes/com/acme/servlet/SimpleServlet.class"));
	}

	private void instrumentClasses(File classesDir) {
	    
		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(TestUtils.project);
		instrumentTask.setDataFile("cobertura.ser");
		FileSet fileSet = new FileSet();
		fileSet.setDir(classesDir.getParentFile());
		fileSet.setIncludes("**/*.class");

		instrumentTask.addFileset(fileSet);
		instrumentTask.execute();
	}

	private void instrumentWar(File warFile) {
	    
		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(TestUtils.project);
		instrumentTask.setDataFile("cobertura.ser");
		FileSet fileSet = new FileSet();
		fileSet.setDir(warFile.getParentFile());
		fileSet.setIncludes("**/*.war");

		instrumentTask.addFileset(fileSet);
		instrumentTask.execute();
	}

	private void generateReportFile() throws IOException {

	    if (xmlReportFile != null) {
	        FileUtils.forceDelete(xmlReportFile);
	    }
	    
		xmlReportFile = temporaryFolder.newFile("coverage.xml");

		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile("cobertura.ser");
		reportTask.setFormat("xml");
		reportTask.setDestDir(new File(xmlReportFile.getParent()));
		reportTask.execute();
	}
}
