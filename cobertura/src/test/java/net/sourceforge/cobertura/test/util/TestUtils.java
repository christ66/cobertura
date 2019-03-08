package net.sourceforge.cobertura.test.util;

import groovy.util.AntBuilder;
import groovy.util.Node;
import groovy.util.XmlParser;
import net.sourceforge.cobertura.ant.InstrumentTask;
import net.sourceforge.cobertura.javancss.ccl.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Path.PathElement;
import org.codehaus.groovy.ant.Groovyc;
import org.xml.sax.SAXException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

import static org.junit.Assert.*;

public class TestUtils {
	private static final String SRC_DIR = "src/main/java";

	static File coberturaClassDir;
	public static final AntBuilder antBuilder = new AntBuilder();
	public static final Project project = new Project();
	public static final String SIMPLE_SOURCE_PATHNAME = "a/mypackage/SimpleSource.java";
	public static final String SOURCE_TEXT = "\n package a.mypackage;" + "\n "
			+ "\n public class SimpleSource {"
			+ "\n 	public void aSimpleMethod() { " + "\n 	}" + "\n }";

	static {
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(consoleLogger);
	}

	public synchronized static File getCoberturaClassDir() {
		if (coberturaClassDir == null) {
            // Should already be compiled
            coberturaClassDir = new File("target/classes");
		}

		return coberturaClassDir;
	}

	public static DirSet getCoberturaClassDirSet() {
		DirSet dirSet = new DirSet();
		dirSet.setDir(getCoberturaClassDir());
		return dirSet;
	}

	public static AntBuilder getCoberturaAntBuilder(File coberturaClassdir) {
		Project project = new Project();
		project.addTaskDefinition("groovyc",
				org.codehaus.groovy.ant.Groovyc.class);
		AntBuilder antBuilder = new AntBuilder(project);

		return antBuilder;
	}

	public static File getTempDir() {
		return new File("target/tmp", "cobertura");
	}

	public static Node getXMLReportDOM(File xmlReport)
			throws ParserConfigurationException, SAXException, IOException {
		return getXMLReportDOM(xmlReport.getAbsolutePath());
	}

	public static Node getXMLReportDOM(String xmlReport)
			throws ParserConfigurationException, SAXException, IOException {
		XmlParser parser = new XmlParser();
		parser
				.setFeature(
						"http://apache.org/xml/features/nonvalidating/load-external-dtd",
						false);
		return parser.parse(xmlReport);
	}

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

	public static int getTotalHitCount(Node dom, String className,
			String methodName) {
		int sum = 0;
		for (Iterator<Node> packagesIterator = dom.iterator(); packagesIterator.hasNext();) {
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
																				sum += Integer.valueOf((String) lineNode.attribute("hits"));
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

		return sum;
	}

    public static double getMethodBranchCoverage(Node dom, String className, String methodName) {
        for (Iterator<Node> packagesIterator = dom.iterator(); packagesIterator.hasNext();) {
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
                                                                return Double.valueOf((String)methodNode.attribute("branch-rate"));
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

        // Could not find branch
        return 0;
    }

    //TODO: remove
	public static void instrumentClasses(AntBuilder ant, File srcDir,
			File datafile, File instrumentDir, Map arguments) {
		FileSet fileSet = new FileSet();
		fileSet.setDir(srcDir);
		fileSet.setIncludes("**/*.class");

		InstrumentTask instrumentTask = new InstrumentTask();
		instrumentTask.setProject(project);
		instrumentTask.addFileset(fileSet);
		instrumentTask.createIncludeClasses().setRegex("mypackage.*");
		instrumentTask.setDataFile(datafile.getAbsolutePath());
		instrumentTask.setToDir(instrumentDir);
		instrumentTask.setThreadsafeRigorous(arguments != null
				&& arguments.containsKey("threadsafeRigorous")
				? (Boolean) arguments.get("threadsafeRigorous")
				: false);
		instrumentTask.setIgnoreTrivial(arguments != null
				&& arguments.containsKey("ignoretrivial") ? (Boolean) arguments
				.get("ignoretrivial") : false);
		instrumentTask.setIgnoreDeprecated(arguments != null
				&& arguments.containsKey("ignoreDeprecated")
				? (Boolean) arguments.get("ignoreDeprecated")
				: false);

		if (arguments != null && arguments.containsKey("ignoreAnnotationNames")) {
			List<String> ignoreAnnotations = (List<String>) arguments
				.get("ignoreAnnotationNames");
			for (String annotation : ignoreAnnotations) {
				instrumentTask.createIgnoreMethodAnnotation()
						.setAnnotationName(annotation);
			}
		}

		if (arguments != null
				&& arguments.containsKey("excludeClassesRegexList")) {
			instrumentTask.createExcludeClasses().setRegex(
					(String) arguments.get("excludeClassesRegexList"));
		}

		instrumentTask.execute();
	}

	public static void compileSource(File srcDir, File destDir) {
		compileSource(srcDir, destDir, "1.7");
	}

    public static void compileSource(File srcDir) {
        compileSource(srcDir, srcDir);
    }

    //TODO: remove
    public static void compileSource(AntBuilder builder, File srcDir) {
        compileSource(srcDir);
    }

    //TODO: remove
    public static void compileSource(AntBuilder builder, File srcDir,
                                     String jdkVersion) {
        compileSource(srcDir, srcDir, jdkVersion);
    }

	public static void compileSource(File srcDir, File destDir,
			String jdkVersion) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Collection<File> files = FileUtils.listFiles(srcDir, new String[]{"java"}, true);

        List jvmArguments = new LinkedList();

        jvmArguments.add("-sourcepath");
        jvmArguments.add(srcDir.getAbsolutePath());
        jvmArguments.add("-g");
        jvmArguments.add("-verbose");
        jvmArguments.add("-d");
        jvmArguments.add(srcDir.getAbsolutePath());
        jvmArguments.add("-source");
        jvmArguments.add(jdkVersion);
        jvmArguments.add("-target");
        jvmArguments.add(jdkVersion);

        for (File file : files) {
            jvmArguments.add(file.getAbsolutePath());
        }

        System.out.println("====== Compiling ======");
        System.out.print("JDK VERSION: ");
        compiler.run(System.in, System.out, System.err, "-version");

        System.out.println("JVM ARGS: " + jvmArguments);

        int result = compiler.run(System.in, System.out, System.err,
                (String[]) jvmArguments.toArray(new String[jvmArguments.size()]));
        if (result == 0) {
            System.out.println("Successful Compilation.");
        } else {
            System.err.println("======= Failed to compile =======");
        }
	}

	public static void compileGroovy(AntBuilder ant, File srcDir) {
		Javac javac = new Javac();
		javac.setDebug(true);

		Groovyc groovyc = new Groovyc();
		groovyc.setProject(project);
		groovyc.setSrcdir(new Path(project, srcDir.getAbsolutePath()));
		groovyc.setDestdir(srcDir);
		groovyc.addConfiguredJavac(javac);
		groovyc.execute();
	}

	public static List<Node> getLineCounts(Node dom, String className,
			String methodName, String signature) {
		List<Node> returnList = new ArrayList<Node>();

		for (Iterator<Node> packagesIterator = dom.iterator(); packagesIterator
				.hasNext();) {
			Node packagesNode = packagesIterator.next();
			if ("packages".equals(packagesNode.name())) {
				for (Iterator<Node> packageIterator = packagesNode.iterator(); packageIterator
						.hasNext();) {
					Node packageNode = packageIterator.next();
					if ("package".equals(packageNode.name())) {
						for (Iterator<Node> classesIterator = packageNode
								.iterator(); classesIterator.hasNext();) {
							Node classesNode = classesIterator.next();
							if ("classes".equals(classesNode.name())) {
								for (Iterator<Node> classIterator = classesNode
										.iterator(); classIterator.hasNext();) {
									Node classNode = classIterator.next();
									if ("class".equals(classNode.name())) {
										if (className.equals(classNode
												.attribute("name"))) {
											for (Iterator<Node> methodsIterator = classNode
													.iterator(); methodsIterator
													.hasNext();) {
												Node methodsNode = methodsIterator
														.next();
												if ("methods"
														.equals(methodsNode
																.name())) {
													for (Iterator<Node> methodIterator = methodsNode
															.iterator(); methodIterator
															.hasNext();) {
														Node methodNode = methodIterator
																.next();
														if ("method"
																.equals(methodNode
																		.name())) {
															if (methodName
																	.equals(methodNode
																			.attribute("name"))) {
																if (signature != null) {
																	returnList
																			.clear();
																} // Keep clearing if signature isn't null
																for (Iterator<Node> linesIterator = methodNode
																		.iterator(); linesIterator
																		.hasNext();) {
																	Node linesNode = linesIterator
																			.next();
																	if ("lines"
																			.equals(linesNode
																					.name())) {
																		for (Iterator<Node> lineIterator = linesNode
																				.iterator(); lineIterator
																				.hasNext();) {
																			Node lineNode = lineIterator
																					.next();
																			if ("line"
																					.equals(lineNode
																							.name())) {
																				returnList
																						.add(lineNode);
																			}
																		}
																	}
																}
																if (signature != null) {
																	if (signature
																			.equals(methodNode
																					.attribute("signature"))) {
																		return returnList;
																	} else {
																		returnList
																				.clear();
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
		return returnList;
	}

	public static File createSourceArchive(File dir) throws IOException {
		File sourceDir = new File(dir, SRC_DIR);
		File sourceFile = new File(sourceDir, SIMPLE_SOURCE_PATHNAME);
		sourceFile.getParentFile().mkdirs();

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(sourceFile));
			bw.write(SOURCE_TEXT);
		} finally {
			if (bw != null)
				bw.close();
		}

		File zipDir = new File(dir, "zip");
		zipDir.mkdirs();
		File zipFile = new File(zipDir, "source.zip");

		Zip zip = new Zip();
		zip.setProject(project);
		zip.setDestFile(zipFile);
		zip.setBasedir(sourceDir);
		zip.execute();

		assertTrue(sourceFile.delete());

		return zipFile;
	}

	public static void instrumentClasses(AntBuilder ant, File srcDir,
			File datafile, File instrumentDir) {
		instrumentClasses(ant, srcDir, datafile, instrumentDir, null);
	}

	public static void checkFrameSummaryHtmlFile(File frameSummaryFile)
			throws Exception {
		XmlParser parser = new XmlParser();
		// the next line is to suppress loading the dtd
		parser
				.setFeature(
						"http://apache.org/xml/features/nonvalidating/load-external-dtd",
						false);

		Node doc = parser.parse(frameSummaryFile);
		List<Node> list = doc.depthFirst();
		int totalClasses = 0;
		int expectedTotalNumberOfClasses = 0;

		for (Iterator<Node> tableIterator = list.iterator(); tableIterator
				.hasNext();) {
			Node tableNode = tableIterator.next();
			if ("table".equals(tableNode.name())) {
				if ("report".equals(tableNode.attribute("class"))) {
					for (Iterator<Node> tbodyIterator = tableNode.iterator(); tbodyIterator
							.hasNext();) {
						Node tbodyNode = tbodyIterator.next();
						if ("tbody".equals(tbodyNode.name())) {
							for (Iterator<Node> trIterator = tbodyNode
									.iterator(); trIterator.hasNext();) {
								Node trNode = trIterator.next();
								if ("tr".equals(trNode.name())) {
									String tdClassString = "td[attributes={class=value}; value=[";
									int tdClassLength = tdClassString.length();

									String value = trNode.value().toString();
									int tdClassFirstOccurance = value
											.indexOf(tdClassString);

									String tdClassStart = value
											.substring(tdClassFirstOccurance);
									int tdClassElement = Integer
											.valueOf(tdClassStart
													.substring(
															tdClassString
																	.length(),
															tdClassStart
																	.indexOf("]],")));
									if (trNode.value().toString().contains(
											"All Packages")) {
										expectedTotalNumberOfClasses = tdClassElement;
									} else {
										totalClasses += tdClassElement;
									}
								}
							}
						}
					}
				}
			}
		}

		assertFalse("All classes should not be 0.",
				0 == expectedTotalNumberOfClasses);

		assertEquals(
				"Class count in All Packages of frame-summary.html does not match the sum of the package class counts",
				expectedTotalNumberOfClasses, totalClasses);
	}

	public static Path getCoberturaDefaultClasspath() {
		Path classpath = new Path(TestUtils.project);
		DirSet dirSetInstrumentDir = new DirSet();
		DirSet dirSetSrcDir = new DirSet();
		dirSetInstrumentDir.setDir(new File(getTempDir(), "instrument"));
		dirSetSrcDir.setDir(new File(getTempDir(), "src"));
		classpath.addDirset(dirSetInstrumentDir);
		classpath.addDirset(dirSetSrcDir);
		classpath.addDirset(TestUtils.getCoberturaClassDirSet());
		// This is needed to fix issue #213
		classpath.add(createDependencyPath("org.slf4j", "slf4j-api", "1.7.5"));
		return classpath;
	}

	/**
	 * Create the path to a dependency in the current user's local Maven repository.
	 * For this to work the local Maven repository must exist in the default location:
	 * <code>${user.home}/.m2/repository</code>
	 *
	 * This is a hack, to get tests that build their own classpath using Ant-classes to work.
	 * Ideally those tests should be rewritten into proper integration tests.
	 *
	 * @param groupId The groupId for the dependency
	 * @param artifactId The artifactId for the dependency
	 * @param version The version for the dependency
	 * @return A Path to the dependency in the local Maven repository
	 */
	public static Path createDependencyPath(String groupId, String artifactId, String version) {
		// Create a path to the current user's local Maven repository
		String userHome = System.getProperty("user.home");
		String localRepository = userHome + File.separator + ".m2" + File.separator + "repository";

		// Create the path to a dependency within the local Maven repository
		String dependencyPath = groupId.replace( ".", File.separator ) + File.separator
			+ artifactId + File.separator + version + File.separator
			+ artifactId + "-" + version + ".jar";

		return new Path(TestUtils.project, localRepository + File.separator + dependencyPath);
	}

	/**
	 * Run Ant's junit task.   Typical usage:
	 * 
	 * 			testUtil.junit(                         Types
	 *				testClass     : 'mypackage.MyTest', String
	 *				ant           : ant,                AntBuilder
	 *				buildDir      : buildDir,           File
	 *				instrumentDir : instrumentDir,      File
	 *				reportDir     : reportDir,          File
	 *			)
	 *
	 * 'ant' is the AntBuilder that is used.   Instrumented classes are in
	 * instrumentDir; The remaining classes are in buildDir.
	 * An XML report is created in reportDir.
	 *
	 * @param hashMap
	 * @return
	 * @throws Exception 
	 */
	public static void junit(HashMap hashMap) throws Exception {
		Path classpath = new Path(project);
		PathElement instDirPathElement = classpath.new PathElement();
		PathElement buildDirPathElement = classpath.new PathElement();
		PathElement coberturaClassDirPathElement = classpath.new PathElement();
		PathElement computerClasspath = classpath.new PathElement();

		FileSet fileSet = new FileSet();

		instDirPathElement.setLocation((File) hashMap.get("instrumentDir"));
		buildDirPathElement.setLocation((File) hashMap.get("buildDir"));
		coberturaClassDirPathElement.setLocation(getCoberturaClassDir());
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
		junit.setProject(project);
		junit.setHaltonfailure(true);
		junit.setDir((File) hashMap.get("buildDir"));
		junit.setFork(true);

		// Add formatter to junitTask
		FormatterElement formatter = new FormatterElement();
		TypeAttribute type = new TypeAttribute();
		type.setValue("xml");
		formatter.setType(type);
		junit.addFormatter(formatter);

		// Add test to junitTask
		JUnitTest test = new JUnitTest((String) hashMap.get("testClass"));
		test.setTodir((File) hashMap.get("reportDir"));
		junit.addTest(test);

		junit.setShowOutput(true);

		// Add classpath to junitTask
		junit.createClasspath().add(classpath);
		System.out.println(classpath);
		// Finally execute junitTask
		junit.execute();
	}

	public static List<Node> getLineCounts(Node dom, String className,
			String methodName) {
		return getLineCounts(dom, className, methodName, null);
	}

	public static void waitForLiveServer(String webContainerHostname,
			int webContainerPort, int timeoutMin) {
		InetSocketAddress address = new InetSocketAddress(webContainerHostname,
				webContainerPort);

		System.out.println("Waiting " + timeoutMin + " min for web server...");

		long beginTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis() + (timeoutMin * 60 * 1000);

		boolean portOpened = false;
		while ((!portOpened) && (System.currentTimeMillis() < endTime)) {
			portOpened = trySocket(address);

			if (portOpened) {
				System.out.println("Web server has opened the port in "
						+ (System.currentTimeMillis() - beginTime) / 1000.0
						/ 60.0 + " min.");
			} else {
				try {
					Thread.sleep(2000); // 2 seconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (!portOpened) {
			throw new RuntimeException(
					"Timed out waiting for webapp server to initialize");
		}
	}

	private static boolean trySocket(InetSocketAddress address) {
		boolean success = false;

		Socket socket = null;
		try {
			socket = new Socket();
			socket.connect(address);
			success = true;
		} catch (ConnectException e) {
			// this is expected
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return success;
	}

	public static boolean isMethodHit(Node dom, String className,
			String methodName) {
		List<Node> methods = TestUtils
				.getLineCounts(dom, className, methodName);
		assertNotNull(methods);
		assertTrue(!methods.isEmpty());

		return Integer.valueOf((String) methods.get(0).attribute("hits")) >= 1;
	}
}
