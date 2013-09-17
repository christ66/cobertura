package net.sourceforge.cobertura.bugs;

import groovy.util.Node;

import java.io.File;

import net.sourceforge.cobertura.ant.ReportTask;
import net.sourceforge.cobertura.coveragedata.TestUnitInformationHolder;
import net.sourceforge.cobertura.test.AbstractCoberturaTestCase;
import net.sourceforge.cobertura.test.util.TestUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class NewFeatureTwitter extends AbstractCoberturaTestCase {

	@Test
	public void test() throws Exception {
		File firstTest = new File(srcDir, "mypackage/FirstTest.java");
		File secondTest = new File(srcDir, "mypackage/SecondTest.java");
		File thirdTest = new File(srcDir, "mypackage/ThirdTest.java");
		File firstTestClass = new File(srcDir, "mypackage/FirstTest.class");
		File secondTestClass = new File(srcDir, "mypackage/SecondTest.class");
		File thirdTestClass = new File(srcDir, "mypackage/ThirdTest.class");
		FileUtils.write(firstTest, test1);
		FileUtils.write(secondTest, test2);
		FileUtils.write(thirdTest, test3);
		FileUtils.write(new File(srcDir, "mypackage/HelloWorld.java"),
				HelloWorld);

		TestUtils.compileSource(ant, srcDir);
		
		//instrument all but the test class (in place)
		super.instrumentClassIndividualTests(false,
											 false,
											 null,
											 ".*Test.*",
											 new String[] {
											 	firstTestClass.getAbsolutePath(),
											 	secondTestClass.getAbsolutePath(),
											 	thirdTestClass.getAbsolutePath()
											 	});

//		super.instrumentClass(false, false, null, ".*Test.*", false);
		// run the MyTest
		super.executeJunitTest("mypackage.FirstTest");

		super.executeJunitTest("mypackage.SecondTest");
		super.executeJunitTest("mypackage.ThirdTest");
		ReportTask reportTask = new ReportTask();
		reportTask.setProject(TestUtils.project);
		reportTask.setDataFile(datafile.getAbsolutePath());
		reportTask.setFormat("xml");
		reportTask.setDestDir(reportDir);
		reportTask.setSrcDir(srcDir.getAbsolutePath());
		reportTask.execute();

		Node dom = TestUtils.getXMLReportDOM(reportDir.getAbsolutePath()
				+ "/coverage.xml");
	}

		String test1 =  "\n package mypackage;" 
					  + "\n "
					  + "\n import org.junit.Test;" 
					  + "\n public class FirstTest {"
					  + "\n   @Test" 
					  + "\n   public void test1(){"
					  + "\n     new HelloWorld();"
					  + "\n   }"
					  + "\n }";

		String test2 =  "\n package mypackage;"
					  + "\n "
					  + "\n import org.junit.Test;"
					  + "\n public class SecondTest {"
					  + "\n   @Test" 
					  + "\n   public void test1(){"
					  + "\n     new HelloWorld();" 
					  + "\n   }"
					  + "\n }";

		String test3 =  "\n package mypackage;"
					  + "\n "
					  + "\n import org.junit.Test;"
					  + "\n public class ThirdTest {"
					  + "\n   @Test"
					  + "\n   public void test1(){"
					  + "\n     new HelloWorld();"
					  + "\n   }"
					  + "\n }";

	String HelloWorld = "\n package mypackage;" 
					  + "\n"
					  + "\n public class HelloWorld {" 
					  + "\n   public HelloWorld() {"
					  + "\n     System.out.println(\"Hello world\");" 
					  + "\n   }" 
					  + "\n }";
}
