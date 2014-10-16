/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Jeremy Thomerson
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2008 Tri Bao Ho
 * Copyright (C) 2009 John Lewis
 * Copyright (C) 2014 Kunal Shah
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
package net.sourceforge.cobertura.reporting;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.javancss.FunctionMetric;
import net.sourceforge.cobertura.javancss.Javancss;
import net.sourceforge.cobertura.javancss.JavancssFactory;

import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Source;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.util.TraceSignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Allows complexity computing for source files, packages and a whole project. Average
 * McCabe's number for methods contained in the specified entity is returned. This class
 * depends on FileFinder which is used to map source file names to existing files.
 * <p/>
 * <p>One instance of this class should be used for the same set of source files - an
 * object of this class can cache computed results.</p>
 *
 * @author Grzegorz Lukasik
 */
public class ComplexityCalculator {
	private static final Logger logger = LoggerFactory
			.getLogger(ComplexityCalculator.class);

	public static final Complexity ZERO_COMPLEXITY = new Complexity();

	// Finder used to map source file names to existing files
	private final FileFinder finder;

	// Factory use to get instances of {@code Javancss}
	private final JavancssFactory javancssFactory;

	// Contains pairs (String sourceFileName, Complexity complexity)
	private Map sourceFileCNNCache = new HashMap();

	// Contains pairs (String packageName, Complexity complexity)
	private Map packageCNNCache = new HashMap();

	// Cache for source file name to its function metrics parsed by {@link Javancss}
	private static final int FILE_FUNCTION_METRIC_CACHE_SIZE = 6;
	private Map<String, List<FunctionMetric>> sourceFileFunctionMetricCache = new LinkedHashMap<String, List<FunctionMetric>>(
		FILE_FUNCTION_METRIC_CACHE_SIZE, 0.75f, true) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, List<FunctionMetric>> arg0) {
			return size() > FILE_FUNCTION_METRIC_CACHE_SIZE;
		}
	};

	private String encoding;

	/**
	 * Creates new calculator. Passed {@link FileFinder} will be used to
	 * map source file names to existing files when needed.
	 *
	 * @param finder {@link FileFinder} that allows to find source files
	 *
	 * @throws NullPointerException if finder is null
	 */
	public ComplexityCalculator(FileFinder finder) {
		this(finder, new JavancssFactory());
	}

	/**
	 * Creates new calculator. Passed {@link FileFinder} will be used to
	 * map source file names to existing files when needed.
	 *
	 * @param finder          {@link FileFinder} that allows to find source files
	 * @param javancssFactory factory to get instances of {@link Javancss}
	 * @throws NullPointerException if finder or javancssFactory is null
	 */
	public ComplexityCalculator(FileFinder finder, JavancssFactory javancssFactory) {
		this.finder = Validate.notNull(finder, "finder should not be null");
		this.javancssFactory = Validate.notNull(javancssFactory, "javancssFactory should not be null");
	}

	/**
	 * Calculates the code complexity number for single source file.
	 * "CCN" stands for "code complexity number."  This is
	 * sometimes referred to as McCabe's number.  This method
	 * calculates the average cyclomatic code complexity of all
	 * methods of all classes in a given directory.
	 *
	 * @param sourceFileName the name of the source file for which you want to calculate the complexity
	 * @return average complexity for the specified source file
	 * @throws IOException
	 */
	private Complexity getAccumlatedCCNForSingleFile(String sourceFileName)
		throws IOException {
		List methodMetrics = getFunctionMetricsForSingleFile(sourceFileName);
		if (methodMetrics.isEmpty()) {
			return ZERO_COMPLEXITY;
		}

		int classCcn = 0;
		for (Iterator method = methodMetrics.iterator(); method.hasNext(); ) {
			FunctionMetric singleMethodMetrics = (FunctionMetric) method.next();
			classCcn += singleMethodMetrics.ccn;
		}

		return new Complexity(classCcn, methodMetrics.size());
	}

	/**
	 * Computes CCN for all sources contained in the project.
	 * CCN for whole project is an average CCN for source files.
	 * All source files for which CCN cannot be computed are ignored.
	 *
	 * @param projectData project to compute CCN for
	 *
	 * @return CCN for project or 0 if no source files were found
	 *
	 * @throws NullPointerException if projectData is null
	 */
	public double getCCNForProject(ProjectData projectData) {
		// Sum complexity for all packages
		Complexity act = new Complexity();

		for (Object pkg : projectData.getPackages()) {
			PackageData packageData = (PackageData) pkg;
			act.add(getCCNForPackageInternal(packageData));
		}

		// Return average CCN for source files
		return act.averageCCN();
	}

	/**
	 * Computes CCN for all sources contained in the specified package.
	 * All source files that cannot be mapped to existing files are ignored.
	 *
	 * @param packageData package to compute CCN for
	 *
	 * @return CCN for the specified package or 0 if no source files were found
	 *
	 * @throws NullPointerException if <code>packageData</code> is <code>null</code>
	 */
	public double getCCNForPackage(PackageData packageData) {
		return getCCNForPackageInternal(packageData).averageCCN();
	}

	private Complexity getCCNForPackageInternal(PackageData packageData) {
		// Return CCN if computed earlier
		Complexity cachedCCN = (Complexity) packageCNNCache.get(packageData
				.getName());
		if (cachedCCN != null) {
			return cachedCCN;
		}

		// Compute CCN for all source files inside package
		Complexity act = new Complexity();
		for (Iterator it = packageData.getSourceFiles().iterator(); it
				.hasNext();) {
			SourceFileData sourceData = (SourceFileData) it.next();
			act.add(getCCNForSourceFileNameInternal(sourceData.getName()));
		}

		// Cache result and return it
		packageCNNCache.put(packageData.getName(), act);
		return act;
	}

	/**
	 * Computes CCN for single source file.
	 *
	 * @param sourceFile source file to compute CCN for
	 *
	 * @return CCN for the specified source file, 0 if cannot map <code>sourceFile</code> to existing file
	 *
	 * @throws NullPointerException if <code>sourceFile</code> is <code>null</code>
	 */
	public double getCCNForSourceFile(SourceFileData sourceFile) {
		return getCCNForSourceFileNameInternal(sourceFile.getName())
				.averageCCN();
	}

	/**
	 * Get the function metrics for the given source file. Use this over the {@link #sourceFileFunctionMetricCache} field.
	 *
	 * @param sourceFileName the name of the source file
	 * @return the function metrics for the given source file (parsed by {@link Javancss})
	 */
	private List<FunctionMetric> getFunctionMetricsForSingleFile(String sourceFileName) {
		List<FunctionMetric> functionMetrics = Collections.emptyList();
		if (!sourceFileFunctionMetricCache.containsKey(sourceFileName)) {
			Source source = null;
			try {
				source = finder.getSource(sourceFileName);
				if (source != null && sourceFileName.endsWith(".java")) {
					Javancss javancss = javancssFactory.newInstance(source.getInputStream(), encoding);
					if (javancss.getLastErrorMessage() != null) {
						//there is an error while parsing the java file. log it
						logger.warn("JavaNCSS got an error while parsing the java "
							+ source.getOriginDesc() + "\n"
							+ javancss.getLastErrorMessage());
					}
					functionMetrics = javancss.getFunctionMetrics();
				}
			} finally {
				if (source != null) {
					source.close();
				}
			}
			sourceFileFunctionMetricCache.put(sourceFileName, functionMetrics);
		}
		return sourceFileFunctionMetricCache.get(sourceFileName);
	}

	private Complexity getCCNForSourceFileNameInternal(String sourceFileName) {
		// Return CCN if computed earlier
		Complexity cachedCCN = (Complexity) sourceFileCNNCache
				.get(sourceFileName);
		if (cachedCCN != null) {
			return cachedCCN;
		}

		// Compute CCN and cache it for further use
		Complexity result = ZERO_COMPLEXITY;
		try {
			result = getAccumlatedCCNForSingleFile(sourceFileName);
		} catch (IOException ex) {
			logger
					.info("Cannot find source file during CCN computation, source=["
							+ sourceFileName + "]");
		}
		sourceFileCNNCache.put(sourceFileName, result);
		return result;
	}

	/**
	 * Computes CCN for source file the specified class belongs to.
	 *
	 * @param classData package to compute CCN for
	 *
	 * @return CCN for source file the specified class belongs to
	 *
	 * @throws NullPointerException if <code>classData</code> is <code>null</code>
	 */
	public double getCCNForClass(ClassData classData) {
		return getCCNForSourceFileNameInternal(classData.getSourceFileName())
				.averageCCN();
	}

	/**
	 * Computes CCN for a method.
	 *
	 * @param classData        class data for the class which contains the method to compute CCN for
	 * @param methodName       the name of the method to compute CCN for
	 * @param methodDescriptor the descriptor of the method to compute CCN for
	 * @return CCN for the method
	 * @throws NullPointerException if <code>classData</code> is <code>null</code>
	 */
	public int getCCNForMethod(ClassData classData, String methodName, String methodDescriptor) {
		Validate.notNull(classData, "classData must not be null");
		Validate.notNull(methodName, "methodName must not be null");
		Validate.notNull(methodDescriptor, "methodDescriptor must not be null");

		int complexity = 0;
		List<FunctionMetric> methodMetrics = getFunctionMetricsForSingleFile(classData.getSourceFileName());

		// golden method = method for which we need ccn
		String goldenMethodName = methodName;
		boolean isConstructor = false;
		if (goldenMethodName.equals("<init>")) {
			isConstructor = true;
			goldenMethodName = classData.getBaseName();
		}
		// fully-qualify the method
		goldenMethodName = classData.getName() + "." + goldenMethodName;
		// replace nested class separator $ by .
		goldenMethodName = goldenMethodName.replaceAll(Pattern.quote("$"), ".");

		TraceSignatureVisitor v = new TraceSignatureVisitor(Opcodes.ACC_PUBLIC);
		SignatureReader r = new SignatureReader(methodDescriptor);
		r.accept(v);

		// for the scope of this method, signature = signature of the method excluding the method name
		String goldenSignature = v.getDeclaration();
		// get parameter type list string which is enclosed by round brackets ()
		goldenSignature = goldenSignature.substring(1, goldenSignature.length() - 1);

		// collect all the signatures with the same method name as golden method
		Map<String, Integer> candidateSignatureToCcn = new HashMap<String, Integer>();
		for (FunctionMetric singleMethodMetrics : methodMetrics) {
			String candidateMethodName = singleMethodMetrics.name.substring(0, singleMethodMetrics.name.indexOf('('));
			String candidateSignature = stripTypeParameters(singleMethodMetrics.name.substring(singleMethodMetrics.name.indexOf('(') + 1,
				singleMethodMetrics.name.length() - 1));
			if (goldenMethodName.equals(candidateMethodName)) {
				candidateSignatureToCcn.put(candidateSignature, singleMethodMetrics.ccn);
			}
		}

		// if only one signature, no signature matching needed
		if (candidateSignatureToCcn.size() == 1) {
			return candidateSignatureToCcn.values().iterator().next();
		}

		// else, do signature matching and find the best match

		// update golden signature using reflection
		if (!goldenSignature.isEmpty()) {
			try {
				String[] goldenParameterTypeStrings = goldenSignature.split(",");
				Class<?>[] goldenParameterTypes = new Class[goldenParameterTypeStrings.length];
				for (int i = 0; i < goldenParameterTypeStrings.length; i++) {
					goldenParameterTypes[i] = ClassUtils.getClass(goldenParameterTypeStrings[i].trim(), false);
				}
				Class<?> klass = ClassUtils.getClass(classData.getName(), false);
				if (isConstructor) {
					Constructor<?> realMethod = klass.getDeclaredConstructor(goldenParameterTypes);
					goldenSignature = realMethod.toGenericString();
				} else {
					Method realMethod = klass.getDeclaredMethod(methodName, goldenParameterTypes);
					goldenSignature = realMethod.toGenericString();
				}
				// replace varargs ellipsis with array notation
				goldenSignature = goldenSignature.replaceAll("\\.\\.\\.", "[]");
				// extract the parameter type list string
				goldenSignature = goldenSignature.substring(goldenSignature.indexOf("(") + 1, goldenSignature.length() - 1);
				// strip the type parameters to get raw types
				goldenSignature = stripTypeParameters(goldenSignature);
			} catch (Exception e) {
				logger.error("Error while getting method CC for " + goldenMethodName, e);
				return 0;
			}
		}
		// replace nested class separator $ by .
		goldenSignature = goldenSignature.replaceAll(Pattern.quote("$"), ".");

		// signature matching - due to loss of fully qualified parameter types from JavaCC, get ccn for the closest match
		double signatureMatchPercentTillNow = 0;
		for (Entry<String, Integer> candidateSignatureToCcnEntry : candidateSignatureToCcn.entrySet()) {
			String candidateSignature = candidateSignatureToCcnEntry.getKey();
			double currentMatchPercent = matchSignatures(candidateSignature, goldenSignature);
			if (currentMatchPercent == 1) {
				return candidateSignatureToCcnEntry.getValue();
			}
			if (currentMatchPercent > signatureMatchPercentTillNow) {
				complexity = candidateSignatureToCcnEntry.getValue();
				signatureMatchPercentTillNow = currentMatchPercent;
			}
		}

		return complexity;
	}

	/**
	 * Strip the type parameters from the signature with parameterized types. Spaces are preserved.
	 * <p/>
	 * E.g. {@code stripTypeParamaters("List<String> a, Map<String, List<? extends Person>> b, int c")} returns {@code "List, Map, int"}.
	 *
	 * @param signature the signature with parameterized types which needs to be stripped
	 * @return the stripped signature
	 */
	private static final String stripTypeParameters(String signature) {
		StringBuilder strippedSignature = new StringBuilder();
		int openIndex = -1;
		int openCount = 0;
		final char open = '<';
		final char close = '>';
		while ((openIndex = signature.indexOf(open)) > -1) {
			strippedSignature.append(signature.substring(0, openIndex));
			for (int i = openIndex + 1; i < signature.length(); i++) {
				if (signature.charAt(i) == close) {
					if (openCount == 0) {
						signature = signature.substring(i + 1);
						break;
					}
					openCount--;
				} else if (signature.charAt(i) == open) {
					openCount++;
				}
			}
		}
		strippedSignature.append(signature);
		return strippedSignature.toString();
	}

	/**
	 * Match the {@code candidate} signature against the {@code golden} signature and return the match confidence.
	 * A signature, for the scope of this method, is just the string of method parameter types; for e.g. signature for method
	 * {@code public void process(List<String> names, int[] scores)} is {@code java.util.List, int[]}.
	 * <p/>
	 * Formula for calculating the return value is:<br/>
	 * Match Confidence = Average of Individual Parameter Type Match Percents<br/>
	 * <p/>
	 * Important: As indicated by the formula, do not compare the confidences across different golden signatures since the confidence values are not
	 * absolute.
	 *
	 * @param candidate signature which is to be matched; can have types which are not fully qualified
	 * @param golden    signature against which {@code candidate} is matched; should have only fully qualified types
	 * @return a value denoting the confidence that the given {@code candidate} signature matches the {@code golden} signature; between 0 and 1; 0
	 * means guaranteed mismatch and a 1 means that guaranteed match.
	 */
	private static final double matchSignatures(String candidate, String golden) {

		// assumption: golden is assumed to have fully qualified types and candidate may have types which are not fully qualified

		String[] candidateParamTypes = candidate.split(",");
		String[] goldenParamTypes = golden.split(",");

		// mismatch: if count of parameters are not same
		if (goldenParamTypes.length != candidateParamTypes.length) {
			return 0;
		}

		int totalParamTypes = goldenParamTypes.length;

		// complete match: if no parameters
		if (totalParamTypes == 0) {
			return 1;
		}

		double totalMatchPercent = 0;

		for (int i = 0; i < totalParamTypes; i++) {
			String goldenParamType = goldenParamTypes[i].trim();
			String candidateParamType = candidateParamTypes[i].trim();

			// mismatch: if golden parameter type is smaller than candidate parameter type
			if (goldenParamType.length() < candidateParamType.length()) {
				return 0;
			}

			if (goldenParamType.equals(candidateParamType)) {
				// complete match
				totalMatchPercent += 1;
			} else {
				int partialMatchIndex = goldenParamType.lastIndexOf(candidateParamType);
				// package separator '.' cannot be before position 1; e.g. a.type
				if (partialMatchIndex > 1 &&
					goldenParamType.length() == (partialMatchIndex + candidateParamType.length()) &&
					goldenParamType.charAt(partialMatchIndex - 1) == '.') {
					// partial match
					totalMatchPercent += (1 - (double) partialMatchIndex / goldenParamType.length());
				} else {
					// mismatch
					return 0;
				}
			}
		}

		return totalMatchPercent / totalParamTypes;
	}

	/**
	 * Set the encoding to be used when reading input streams.
	 * 
	 * @param encoding
	 *            The encoding to use
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Represents complexity of source file, package or project. Stores the number of
	 * methods inside entity and accumlated complexity for these methods.
	 */
	private static class Complexity {
		private double accumlatedCCN;
		private int methodsNum;

		public Complexity(double accumlatedCCN, int methodsNum) {
			this.accumlatedCCN = accumlatedCCN;
			this.methodsNum = methodsNum;
		}

		public Complexity() {
			this(0, 0);
		}

		public double averageCCN() {
			if (methodsNum == 0) {
				return 0;
			}
			return accumlatedCCN / methodsNum;
		}

		public void add(Complexity second) {
			accumlatedCCN += second.accumlatedCCN;
			methodsNum += second.methodsNum;
		}
	}
}
