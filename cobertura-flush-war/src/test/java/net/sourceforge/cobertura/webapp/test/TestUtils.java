package net.sourceforge.cobertura.webapp.test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.xml.sax.SAXException;

import groovy.util.Node;
import groovy.util.XmlParser;

/**
 * FIXME: Duplicated from cobertura module.
 */
public class TestUtils {

	public static final Project project = new Project();
	
	static {
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(consoleLogger);
	}

	public static Node getXMLReportDOM(File xmlReport)
			throws ParserConfigurationException, SAXException, IOException {
	    
		return getXMLReportDOM(xmlReport.getAbsolutePath());
	}

	public static Node getXMLReportDOM(String xmlReport)
			throws ParserConfigurationException, SAXException, IOException {
	    
		final XmlParser parser = new XmlParser();

		parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		
		return parser.parse(xmlReport);
	}

	@SuppressWarnings("unchecked")
    public static int getHitCount(Node dom, String className, String methodName) {
	    
		for (Iterator<Node> packagesIterator = dom.iterator(); packagesIterator
				.hasNext();) {
			Node packagesNode = packagesIterator.next();
			if ("packages".equals(packagesNode.name())) {
				for (Iterator<Node> packageIterator = packagesNode.iterator(); packageIterator.hasNext();) {
					Node packageNode = packageIterator.next();
					if ("package".equals(packageNode.name())) {
						for (Iterator<Node> classesIterator = packageNode.iterator(); classesIterator.hasNext();) {
							Node classesNode = classesIterator.next();
							if ("classes".equals(classesNode.name())) {
								for (Iterator<Node> classIterator = classesNode.iterator(); classIterator.hasNext();) {
									Node classNode = classIterator.next();
									if ("class".equals(classNode.name())) {
										if (className.equals(classNode.attribute("name"))) {
											for (Iterator<Node> methodsIterator = classNode.iterator(); methodsIterator.hasNext();) {
												Node methodsNode = methodsIterator.next();
												if ("methods".equals(methodsNode.name())) {
													for (Iterator<Node> methodIterator = methodsNode.iterator(); methodIterator.hasNext();) {
														Node methodNode = methodIterator.next();
														if ("method".equals(methodNode.name())) {
															if (methodName.equals(methodNode.attribute("name"))) {
																for (Iterator<Node> linesIterator = methodNode.iterator(); linesIterator.hasNext();) {
																	Node linesNode = linesIterator.next();
																	if ("lines".equals(linesNode.name())) {
																		for (Iterator<Node> lineIterator = linesNode.iterator(); lineIterator.hasNext();) {
																			Node lineNode = lineIterator.next();
																			if ("line".equals(lineNode.name())) {
																				return Integer.valueOf((String) lineNode.attribute("hits"));
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return 0;
	}
}
