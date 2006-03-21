/* Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 John Lewis
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License 1.1 (so that it can be used from both the main
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * This class controls access to any file so that multiple JVMs will
 * not be able to write to the file at the same time.
 *
 * A file called "filename.lock" is created and Java's FileLock class
 * is used to lock the file.
 *
 * @author John Lewis
 *
 */
public class FileLocker
{

	/**
	 * A file called "filename.lock" that resides in the same directory
	 * as "filename"
	 */
	private FileLock lock;

	private FileChannel lockChannel;
	private File lockFile;

	public FileLocker(File file)
	{
		String lockFileName = file.getName() + ".lock";
		File parent = file.getParentFile();
		if (parent == null)
		{
			lockFile = new File(lockFileName);
		}
		else
		{
			lockFile = new File(parent, lockFileName);
		}
		lockFile.deleteOnExit();
	}

	/**
	 * Obtains a lock on the file.  This blocks until the lock is obtained.
	 */
	public boolean lock()
	{
		try
		{
			lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Unable to get lock channel for " + lockFile.getAbsolutePath()
					+ ": " + e.getLocalizedMessage());
			return false;
		}

		try
		{
			lock = lockChannel.lock();
		}
		catch (IOException e)
		{
			System.err.println("Unable to get lock on " + lockFile.getAbsolutePath() + ": "
					+ e.getLocalizedMessage());
			return false;
		}

		return true;
	}

	/**
	 * Releases the lock on the file.
	 */
	public void release()
	{
		lock = releaseFileLock(lock);
		lockChannel = closeChannel(lockChannel);
		lockFile.delete();
	}

	private static FileLock releaseFileLock(FileLock lock)
	{
		if (lock != null)
		{
			try
			{
				if (lock.isValid())
				{
					lock.release();
				}
				lock = null;
			}
			catch (Throwable t)
			{
				System.err.println("Unable to release locked file: " + t.getLocalizedMessage());
			}
		}
		return lock;
	}

	private static FileChannel closeChannel(FileChannel channel)
	{
		if (channel != null)
		{
			try
			{
				if (channel.isOpen())
				{
					channel.close();
				}
				channel = null;
			}
			catch (Throwable t)
			{
				System.err.println("Unable to close file channel: " + t.getLocalizedMessage());
			}
		}
		return channel;
	}

}
