/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Grzegorz Lukasik
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2006 Jiri Mares
 * Copyright (C) 2008 Scott Frederick
 * Copyright (C) 2010 Tad Smith 
 * Copyright (C) 2010 Piotr Tabor  
 * Contact information for the above is given in the COPYRIGHT file.
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

package net.sourceforge.cobertura.instrument;

/**
 * Deprecated wrapper class for the new CheckCoverageMain class.  It is only
 * here to facilitate an orderly transition to the new class and will go
 * away in a few releases
 *
 * @author Steven C. Saliman
 */
@Deprecated
public class Main {

	public static void main(String[] args) {
		System.err.println("net.sourceforge.cobertura.instrument.Main is a deprecated class.");
		System.err.println("Please use net.sourceforge.cobertura.instrument.InstrumentMain instead");
		InstrumentMain.main(args);
	}
}
