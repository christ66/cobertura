package net.sourceforge.cobertura.jdk;

import groovy.util.AntBuilder;
import groovy.util.Node;
import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.test.util.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Java;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * @author stevenchristou
 *         Date: 3/10/16
 *         Time: 1:19 AM
 */
public class JDKUtils {
    static AntBuilder ant = TestUtils.getCoberturaAntBuilder(TestUtils
            .getCoberturaClassDir());

    public static Node setUpBeforeClass(String testFile, String jdkVersion, String className, String classFile) throws IOException,
            ParserConfigurationException, SAXException {
        FileUtils.deleteQuietly(TestUtils.getTempDir());

		/*
		 * First create the junit test structure.
		 */
        File tempDir = TestUtils.getTempDir();
        File srcDir = new File(tempDir, "src");
        File instrumentDir = new File(tempDir, "instrument");

        File mainSourceFile = new File(srcDir, classFile);

        File datafile = new File(srcDir, "cobertura.ser");
        mainSourceFile.getParentFile().mkdirs();

        FileUtils.write(mainSourceFile, testFile);

		/*
		 * Next let's compile the test code we just made.
		 */
        TestUtils.compileSource(ant, srcDir, jdkVersion);

		/*
		 * Let's now instrument all the classes. In this case we instrument with default items.
		 */
        TestUtils.instrumentClasses(ant, srcDir, datafile, instrumentDir);

		/*
		 * Kick off the Main (instrumented) class.
		 */
        Java java = new Java();
        java.setProject(TestUtils.project);
        java.setClassname(className);
        java.setDir(srcDir);
        java.setFork(true);
        java.setJVMVersion(jdkVersion);
        java.setFailonerror(true);
        java.setClasspath(TestUtils.getCoberturaDefaultClasspath());
        java.execute();

		/*
		 * Now create a cobertura xml file and make sure the correct counts are in it.
		 */
        ReportTask reportTask = new ReportTask();
        reportTask.setProject(TestUtils.project);
        reportTask.setDataFile(datafile.getAbsolutePath());
        reportTask.setFormat("xml");
        reportTask.setDestDir(srcDir);
        reportTask.execute();

		/*
		 *
		 */
        System.out.println(srcDir.getAbsolutePath());
        return TestUtils.getXMLReportDOM(srcDir.getAbsolutePath()
                + "/coverage.xml");
    }

}
