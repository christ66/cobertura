/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2006 Mark Doliner
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

package net.sourceforge.cobertura.instrument;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * This class represents an archive within an archive.
 *
 * @author John Lewis
 */
class Archive {

	private byte[] bytes;
	private boolean modified;
	private CoberturaFile file;

	/**
	 * Create an object that holds a buffer to an archive that is within a parent archive.
	 *
	 * @param file  The parent archive on the hard drive that holds the child archive.
	 * @param bytes The contents of the child archive.
	 */
	Archive(CoberturaFile file, byte[] bytes) {
		this.bytes = bytes;
		this.file = file;
	}

	/**
	 * Return an input stream for the contents of this archive (the child).
	 *
	 * @return An InputStream for the contents.
	 */
	InputStream getInputStream() {
		return new ByteArrayInputStream(this.bytes);
	}

	/**
	 * Set this archive's bytes after they have been modified via instrumentation.
	 *
	 * @param bytes The new contents of the archive (instrumented).
	 */
	void setModifiedBytes(byte[] bytes) {
		this.bytes = bytes;
		this.modified = true;
	}

	/**
	 * Return true if this archive has been modified (instrumented).
	 *
	 * @return true if modified.
	 */
	boolean isModified() {
		return modified;
	}

	/**
	 * Return the contents of this archive.
	 *
	 * @return A byte array with the contents of this archive.
	 */
	byte[] getBytes() {
		return this.bytes;
	}

	/**
	 * Returns the parent archive that contains this archive.
	 *
	 * @return A CoberturaFile representing the parent archive.
	 */
	CoberturaFile getCoberturaFile() {
		return this.file;
	}
}
