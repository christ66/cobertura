/**
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner <thekingant@users.sourceforge.net>
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

package net.sourceforge.cobertura.coverage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.log4j.Logger;

/**
 * Add coverage instrumentation to an existing class. Instances of
 * this class are normally created by @see Main, as part of the
 * instrumentation process.
 */
class InstrumentClassGen extends ClassGen
{

	private static final Logger logger = Logger
			.getLogger(InstrumentClassGen.class);

	/**
	 * The set of method names concatenated with their signature.
	 */
	private final Set methodNamesAndSignatures = new HashSet();

	/**
	 * The set of "real" source line numbers that are present in this
	 * class. That is, those lines of Java source code that do not
	 * represent comments, or other syntax "fluff" (e.g., "} else {"),
	 * or those lines that have been ignored because they match the
	 * ignore regex.
	 */
	private final Set sourceLineNumbers = new HashSet();

	/**
	 * A mapping from method name and signature to the set of line
	 * numbers for that method.
	 */
	private final Map methodLineNumbers = new HashMap();

	/**
	 * A set of all conditionals for this class.
	 * @see Conditional
	 */
	private final Set sourceConditionals = new HashSet();

	/**
	 * A mapping from method name and signature to the set of
	 * conditionals for that method.
	 * @see Conditional
	 */
	private final Map methodConditionals = new HashMap();

	private final String ignoreRegex;

	InstrumentClassGen(JavaClass javaClass, String ignoreRegex)
	{
		/**
		 * Copy everything from the original class
		 */
		super(javaClass);

		this.ignoreRegex = ignoreRegex;
	}

	/**
	 * Add coverage instrumentation to the class. Once instrumented, the
	 * instrumented class is tagged with a marker interface @see
	 * HasBeenInstrumented to prevent it from being instrumented again.
	 */
	void addInstrumentation()
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("adding instrumentation to: " + getClassName());
		}

		/**
		 * Add instrumentation to all methods in this class
		 */
		instrumentMethods(getMethods());

		/**
		 * Make this class implement the "HasBeenInstrumented" interface.
		 * This is essentially a fancy way for Cobertura to set a flag
		 * meaning "this class has been instrumented."
		 */
		addInterface(HasBeenInstrumented.class.getName());
	}

	/**
	 * Add instrumentation to the given methods.
	 *
	 * @param methods A list of methods in the bytecode of
	 *        this class.
	 */
	private void instrumentMethods(Method[] methods)
	{
		for (int i = 0; i < methods.length; i++)
		{
			try
			{
				instrumentMethod(methods[i]);
			}
			catch (Exception e)
			{
				logger.error(
						"Caught an exception when instrumenting the method "
								+ methods[i].getName()
								+ "--ignoring method.  Stacktrace is", e);
			}
		}
	}

	/**
	 * Add instrumentation to the given method.
	 *
	 * @param method The bytecode representation of a method.
	 */
	private void instrumentMethod(Method method)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("adding instrumentation to: " + getClassName() + '.'
					+ method.getName());
		}
		InstrumentMethodGen instrumentMethodGen = new InstrumentMethodGen(
				method, this, ignoreRegex);
		instrumentMethodGen.addInstrumentation();
		Method instrumentedMethod = instrumentMethodGen.getMethod();
		replaceMethod(method, instrumentedMethod);
		add(instrumentMethodGen);
	}

	/**
	 * Add coverage data collected during the instrumentation to this class.
	 */
	private void add(InstrumentMethodGen instrument)
	{
		methodNamesAndSignatures.add(instrument.getMethodNameAndSignature());
		sourceLineNumbers.addAll(instrument.getSourceLineNumbers());
		methodLineNumbers.put(instrument.getMethodNameAndSignature(),
				instrument.getSourceLineNumbers());
		sourceConditionals.addAll(instrument.getConditionals());
		methodConditionals.put(instrument.getMethodNameAndSignature(),
				instrument.getConditionals());
	}

	/**
	 * @return the set of method names and signatures that can be found
	 * in this class.
	 */
	Set getMethodNamesAndSignatures()
	{
		return methodNamesAndSignatures;
	}

	/**
	 * @return the set of source line numbers for this class
	 */
	Set getSourceLineNumbers()
	{
		return sourceLineNumbers;
	}

	/**
	 * @return a mapping from method name and signature to the set of
	 * line numbers for that method.
	 */
	Map getMethodLineNumbers()
	{
		return methodLineNumbers;
	}

	/**
	 * @return The set of source conditionals for this class.
	 * @see Conditional
	 */
	Set getSourceConditionals()
	{
		return sourceConditionals;
	}

	/**
	 * @return a mapping from method name and signature to the set of
	 * conditionals for that method.
	 * @see Conditional
	 */
	Map getMethodConditionals()
	{
		return methodConditionals;
	}

}