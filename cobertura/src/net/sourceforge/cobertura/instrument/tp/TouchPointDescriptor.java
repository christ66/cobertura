/*
 * Cobertura - http://cobertura.sourceforge.net/
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

package net.sourceforge.cobertura.instrument.tp;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Root class for all touch-points (points in source-code that we want to monitor)
 * 
 * @author piotr.tabor@gmail.com
 *
 */
public abstract class TouchPointDescriptor {
	
	public TouchPointDescriptor(int eventId, int lineNumber) {
		this.eventId = eventId;
		this.lineNumber = lineNumber;
	}

	/**
	 * eventId (asm code identifier) of the interesting instruction
	 */
	private int eventId;
	
	/**
	 * Number of line in which the touch-point is localized 
	 */
	private int lineNumber;
	
	/**
	 * @return eventId (asm code identifier) of the interesting instruction
	 */
	public int getEventId() {
		return eventId;
	}
	/**
	 * Sets eventId (asm code identifier) of the interesting instruction
	 */

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
	
	/**
	 * @return number of line in which the touch-point is localized
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Sets number of line in which the touch-point is localized
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	/**
	 * Every touch-point will have assigned some counters. This methods assigne the ids to the touch-point
	 * using given idGenerator 

	 * @param idGenerator
	 * 
	 * @return number of used 'ids' for the touch-point.  
	 */
	public abstract int assignCounters(AtomicInteger idGenerator);
}
