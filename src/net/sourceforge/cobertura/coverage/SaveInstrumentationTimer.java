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

import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Timer task to save the instrumentation to disk.
 */
class SaveInstrumentationTimer extends TimerTask
{

	private static final Logger logger = Logger
			.getLogger(SaveInstrumentationTimer.class);

	final InstrumentationPersistence instrumentationPersistence;

	SaveInstrumentationTimer(
			InstrumentationPersistence instrumentationPersistence)
	{
		this.instrumentationPersistence = instrumentationPersistence;
	}

	public void run()
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("save instrumentation task has started");
		}

		instrumentationPersistence.saveInstrumentation();

		if (logger.isInfoEnabled())
		{
			logger.info("saved: "
					+ instrumentationPersistence.keySet().size() + " items.");
		}
	}
}