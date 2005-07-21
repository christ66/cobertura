/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Grzegorz Lukasik
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

package net.sourceforge.cobertura.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * Helper class with useful I/O operations.
 * 
 * @author Grzegorz Lukasik
 */
public abstract class IOUtil
{

	private static final Logger logger = Logger.getLogger(IOUtil.class);

	/**
	 * Copies bytes from input stream into the output stream.  Stops
	 * when the input stream read method returns -1.  Does not close
	 * the streams.
	 * 
	 * @throws IOException If either passed stream will throw IOException.
	 * @throws NullPointerException If either passed stream is null.
	 */
	public static void copyStream(InputStream in, OutputStream out)
			throws IOException
	{
		// NullPointerException is explicity thrown to guarantee expected behaviour
		if (in == null || out == null)
			throw new NullPointerException();

		int el;
		byte[] buffer = new byte[1 << 15];
		while ((el = in.read(buffer)) != -1)
		{
			out.write(buffer, 0, el);
		}
	}

	/**
	 * Returns an array that contains values read from the
	 * given input stream.
	 * 
	 * @throws NullPointerException If null stream is passed.
	 */
	public static byte[] createByteArrayFromInputStream(InputStream in)
			throws IOException
	{
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		copyStream(in, byteArray);
		return byteArray.toByteArray();
	}

	/**
	 * Moves a file from one location to other.
	 *
	 * @throws IOException If IO exception occur during moving.
	 * @throws NullPointerException If either passed file is null.
	 */
	public static void moveFile(File sourceFile, File destinationFile)
			throws IOException
	{
		logger.debug("Moving " + sourceFile.getAbsolutePath() + " to "
				+ destinationFile.getAbsolutePath());

		// Move file using File method if possible
		boolean succesfulMove = sourceFile.renameTo(destinationFile);
		if (succesfulMove)
			return;

		// Copy file from source to destination
		logger.debug("Using copy and delete method");
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = new FileInputStream(sourceFile);
			out = new FileOutputStream(destinationFile);
			copyStream(in, out);
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
				}
			}
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		// Remove source file
		sourceFile.delete();
	}

}
