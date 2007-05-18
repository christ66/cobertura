/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Jeremy Thomerson
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

package someotherpackage;

/**
 * This class is only for testing that stuff from multiple source
 * directories works properly.
 *  
 * @author Jeremy Thomerson
 */
public class SomeOtherClass {
	
	private int counter;

	public SomeOtherClass() {
		// no-op
	}

	public int incrementCounter() {
		return ++counter;
	}
	
	public int decrementCounter() {
		return --counter;
	}
	
	public int getCounter() {
		return counter;
	}
	
	/**
	 * Don't call this method.  It is one that is supposed to not be called
	 * by the unit tests so that we can verify that everything is being
	 * recorded properly. 
	 */
	public void neverCallThisMethod() {
		throw new UnsupportedOperationException("You weren't supposed to call this method.");
	}
}
