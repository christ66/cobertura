/**
 * 
 */
package net.sourceforge.cobertura.webapp.test

import net.sourceforge.cobertura.test.util.TestUtil
import net.sourceforge.cobertura.test.util.WebappServer

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

public class WebAppFunctionalTest {
	
	def ant = TestUtil.antBuilder
	
	
	@Before
	void setUp() {
	}
	
	@Test
	void basicStartAndStopOfWebApp() {
		TestUtil.withTempDir { tempDir ->
			def webappServerDir = new File(tempDir, "webserver")
			def srcDir = new File(tempDir, "src")
			
			WebappServer.writeSimpleServletSource(srcDir)
			
			def appName = "simple"

			def webappServer = new WebappServer(dir:webappServerDir)

			webappServer.deployApp(
					webInfText:WebappServer.SIMPLE_SERVLET_WEB_XML_TEXT,
					srcDir:srcDir,
					appName:appName,
					instrumentRegEx:'com.acme.*')
					
			def data = webappServer.withRunningServer { data ->
			
				//do a HTTP get so the doGet method will be hit
				def webappResponse = (new java.net.URL("http://${data.hostname}:${data.webappPort}/${appName}/SimpleServlet")).text
				assertEquals("Webapp response was incorrect", "Hi", webappResponse.trim())

				data.coberturaAnt.'cobertura-report'(datafile:data.datafile, format:'xml', destdir:data.xmlReport.getParent())
				def dom = TestUtil.getXMLReportDOM(data.xmlReport)
				
				//make sure the report shows the doGet method has not been hit yet - the data is not flushed until the server stops
				assertFalse(TestUtil.isMethodHit(dom, "doGet"))
			}
			
			//now that the server has stopped, make sure the report shows it has been hit
			assertTrue("doGet has hits=0 in cobertura report", TestUtil.isMethodHit(data.dom, "doGet"))
		}
	}
}
