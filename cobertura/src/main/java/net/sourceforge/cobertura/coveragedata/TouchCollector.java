/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2010 Piotr Tabor
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License (so that it can be used from both the main
 * Cobertura classes and the ant tasks).
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

package net.sourceforge.cobertura.coveragedata;

import net.sourceforge.cobertura.CoverageIgnore;
import net.sourceforge.cobertura.instrument.pass3.AbstractCodeProvider;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@CoverageIgnore
public class TouchCollector {
	private static final Logger logger = Logger.getLogger(TouchCollector.class
			.getCanonicalName());
	/*In fact - concurrentHashset*/
	private static Map<Class<?>, Integer> registeredClasses = new ConcurrentHashMap<Class<?>, Integer>();

	static {
		ProjectData.getGlobalProjectData(); // To call ProjectData.initialize();
	}

	public static synchronized void registerClass(Class<?> classa) {
		registeredClasses.put(classa, 0);
	}

	/**
	 * This method is only for backward compatibility
	 * 
	 * Information:
	 * ASM version 4.1 does not allow for the data type java.lang.Class to be a parameter
	 * to the method visitLdcInsn which causes issues for anything below .class versions
	 * 49 and lower. Changing the registered class to use instead a String parameter and
	 * search for the class in the classpath helped resolve the issue.
	 * Also as a side note: The replace parameters might enter as "java/lang/String" and
	 * need to be translated to "java.lang.String" so the forName method can understand it.
	 * 
	 * @param classa Class that needs to be registered.
	 * @throws ClassNotFoundException 
	 */
	public static synchronized void registerClass(String classa)
			throws ClassNotFoundException {
		try {
			// If it's not in the system jvm, then search the current thread for the class.
			// This is a dirty hack to guarantee that multiple classloaders can invoke cobertura code.

			// We try 2 methods to register the classes
			// First method we try to call the invoker classloader. If the invoker causes an exception (NoClassDefFound) it
			// will then call Thread.currentThread.getContextClassLoader() which gets the current threads classloader and
			// checks to see if cobertura code is in there. This is here because there are situations where multiple
			// classloaders might be invoked and it requires the check of multiple classloaders.

			boolean found = false;
			Class<?> clazz = Class.forName(classa.replace("/", "."), false,
					Thread.currentThread().getContextClassLoader());
			for (Method meth : clazz.getMethods()) {
				if (meth.toString().contains("net.sourceforge.cobertura")) {
					registerClass(clazz);
					found = true;
				}
			}

			if (!found) {
				clazz = Class.forName(classa.replace("/", "."));
				registerClass(clazz);
			}
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Exception when registering class: "
					+ classa, e);
			throw e;
		}
	}

	public static synchronized void applyTouchesOnProjectData(
			ProjectData projectData) {
		logger
				.fine("=================== START OF REPORT ======================== ");
		for (Class<?> c : registeredClasses.keySet()) {
			logger.fine("Report: " + c.getName());
			ClassData cd = projectData.getOrCreateClassData(c.getName());
			applyTouchesToSingleClassOnProjectData(cd, c);
		}
		logger
				.fine("===================  END OF REPORT  ======================== ");
	}

	private static final Object[] NO_ARGS = new Object[0];

	private static void applyTouchesToSingleClassOnProjectData(
			final ClassData classData, final Class<?> c) {
		logger.finer("----------- " + c.getCanonicalName()
				+ " ---------------- ");

		try {
			Method m0 = c
					.getDeclaredMethod(AbstractCodeProvider.COBERTURA_GET_AND_RESET_COUNTERS_METHOD_NAME);
			m0.setAccessible(true);
			final Object res = m0.invoke(null, NO_ARGS);

			LightClassmapListener lightClassmap = null;
			if (res instanceof int[]) {
				lightClassmap = new ApplyToClassDataLightClassmapListener(
						classData, (int[]) res);
			} else /* if(res instanceof TestUnitInformationHolder[]) */{
				lightClassmap = new ApplyToClassDataLightClassmapListener(
						classData, (TestUnitInformationHolder[]) res);
			}

			Method m = c.getDeclaredMethod(
					AbstractCodeProvider.COBERTURA_CLASSMAP_METHOD_NAME,
					LightClassmapListener.class);
			m.setAccessible(true);
			m.invoke(null, lightClassmap);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot apply touches", e);
		}
	}

	@CoverageIgnore
	private static class ApplyToClassDataLightClassmapListener
			implements
				LightClassmapListener {
		private final ClassData classData;
		private final int[] res;

		private int currentLine = 0;
		private int jumpsInLine = 0;
		private int switchesInLine = 0;

		private void updateLine(int new_line) {
			if (new_line != currentLine) {
				currentLine = new_line;
				jumpsInLine = 0;
				switchesInLine = 0;
			}
		}

		public ApplyToClassDataLightClassmapListener(ClassData cd, int[] res) {
			classData = cd;
			this.res = res;
		}

		public ApplyToClassDataLightClassmapListener(ClassData cd,
				TestUnitInformationHolder[] results) {
			classData = cd;

			this.res = new int[results.length];
			for (int i = 0; i < results.length; i++) {
				this.res[i] = results[i].getNumOfExecutions();
			}
		}

		public void setSource(String source) {
			logger.fine("source: " + source);
			classData.setSourceFileName(source);

		}

		public void setClazz(Class<?> clazz) {
		}

		public void setClazz(String clazz) {
		}

		public void putLineTouchPoint(int classLine, int counterId,
				String methodName, String methodDescription) {
			updateLine(classLine);
			LineData ld = classData.addLine(classLine, methodName,
					methodDescription);
			ld.touch(res[counterId]);
		}

		public void putSwitchTouchPoint(int classLine, int maxBranches,
				int... counterIds) {
			updateLine(classLine);
			LineData ld = getOrCreateLine(classLine);
			int switchId = switchesInLine++;
			classData.addLineSwitch(classLine, switchId, 0,
					counterIds.length - 2, maxBranches);
			for (int i = 0; i < counterIds.length; i++) {
				ld.touchSwitch(switchId, i - 1, res[counterIds[i]]);
			}
		}

		public void putJumpTouchPoint(int classLine, int trueCounterId,
				int falseCounterId) {
			updateLine(classLine);
			LineData ld = getOrCreateLine(classLine);
			int branchId = jumpsInLine++;
			classData.addLineJump(classLine, branchId);
			ld.touchJump(branchId, true, res[trueCounterId]);
			ld.touchJump(branchId, false, res[falseCounterId]);
		}

		private LineData getOrCreateLine(int classLine) {
			LineData ld = classData.getLineData(classLine);
			if (ld == null) {
				ld = classData.addLine(classLine, null, null);
			}
			return ld;
		}
	}
}
