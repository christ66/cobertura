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
package net.sourceforge.cobertura.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * This class allows you to add multiple source directories
 * and file paths to it, and it will find in which of those
 * base directories the file exists.
 *  
 * @author Jeremy Thomerson
 */
public class FileFinder {

	private static Logger LOGGER = Logger.getLogger(FileFinder.class);
	private List baseDirectories = new ArrayList();
	private List sourceFilePaths = new ArrayList();
	
	private List cached = null;
	private boolean changed;
	
	public FileFinder() {
		// no-op
	}
	
	public void addBaseDirectory(File path) {
		change();
		baseDirectories.add(path);
	}

	public void addSourceFilePath(String path) {
		change();
		sourceFilePaths.add(path);
	}
	
	public List getBaseDirectories() {
		return new ArrayList(baseDirectories);
	}
	
	public List getFilePaths() {
		compute();
		return new ArrayList(cached);
	}
	
	public File findFile(String filePart) {
		compute();
		List mine = Collections.EMPTY_LIST;
		synchronized(this) {
			mine = new ArrayList(cached);
		}
		for (Iterator it = mine.iterator(); it.hasNext(); ) {
			String path = (String) it.next();
			if (path.replace('/', '\\').endsWith(filePart.replace('/', '\\'))) {
				return new File(path);
			}
		}
		return null;
	}
	
	private synchronized void change() {
		changed = true;
	}
	
	private synchronized boolean isChanged() {
		return changed;
	}
	
	private synchronized void compute() {
		if (isChanged()) {
			List results = new ArrayList(sourceFilePaths.size());
			for (Iterator it = sourceFilePaths.iterator(); it.hasNext(); ) {
				String filePart = (String) it.next();
				String path = getPath(filePart);
				if (path != null) {
					results.add(path);
				} else {
					LOGGER.warn("File not found for: " + filePart + "(base directories: " + baseDirectories + ")");
				}
			}
			cached = results;
			changed = false;
		}
	}
	
	private String getPath(String path) {
		String result = null;
		for (Iterator it = baseDirectories.iterator(); it.hasNext(); ) {
			File baseDir = (File) it.next();
			File file = new File(baseDir, path);
			if (file.exists()) {
				result = file.getAbsolutePath();
			}
		}
		
		return result;
	}
}
