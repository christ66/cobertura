/*
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

import java.util.Timer;

import org.apache.log4j.Logger;

/**
 * <p>
 * This class implements HasBeenInstrumented so that when cobertura
 * instruments itself, it will omit this class.  It does this to
 * avoid an infinite recursion problem because instrumented classes
 * make use of this class.
 * </p>
 */
public class CoverageDataFactory extends InstrumentationPersistence
		implements Runnable, HasBeenInstrumented
{

	private static final Logger logger = Logger
			.getLogger(CoverageDataFactory.class);
	static final CoverageDataFactory instrumentationFactory = new CoverageDataFactory();

	Timer timer = new Timer(true);

	private CoverageDataFactory()
	{
		merge(loadInstrumentation());

		if (logger.isInfoEnabled())
		{
			logger.info("loaded: " + keySet().size() + " items.");
		}

		/*
		 * TODO:
		 * I suspect this causes problems when
		 * net.sourceforge.cobertura.instrumentation.interval
		 * is not equal to 0.  It seems like it deletes cobertura.ser just as
		 * it begins running the first test.  So then we only have coverage
		 * data about the lines we tests.  Also, attempting to load this as
		 * we begin testing prints an ugly file not found exception... Hmm,
		 * what's going on here?  It should load with no problems, I think.
		 * It looks like it's trying to load the file from the wrong place,
		 * or something.
		 */
		if (getInstrumentationInterval() > 0)
		{
			timer.schedule(new SaveInstrumentationTimer(this),
					getInstrumentationInterval(),
					getInstrumentationInterval());
		}

		Runtime.getRuntime().addShutdownHook(new Thread(this));
	}

	int getInstrumentationInterval()
	{
		return Integer.getInteger(
				"net.sourceforge.cobertura.instrumentation.interval", 0)
				.intValue() * 1000;
	}

	public void run()
	{
		if (logger.isInfoEnabled())
		{
			logger.info("shutdown hook started");
		}

		saveInstrumentation();

		if (logger.isInfoEnabled())
		{
			logger.info("saved: " + keySet().size() + " items.");
		}

		if (logger.isInfoEnabled())
		{
			logger.info("shutdown hook has finished");
		}
	}

	public static CoverageDataFactory getInstance()
	{
		return instrumentationFactory;
	}

	public CoverageData newInstrumentation(String className)
	{
		if (!instrumentation.containsKey(className))
		{
			instrumentation.put(className, new CoverageData());
		}
		return (CoverageData)instrumentation.get(className);
	}
}