/* Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 John Lewis
 * Copyright (C) 2006 Mark Doliner
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
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class controls access to any file so that multiple JVMs will
 * not be able to write to the file at the same time.
 *
 * A file called "filename.lock" is created and Java's FileLock class
 * is used to lock the file.
 *
 * The java.nio classes were introduced in Java 1.4, so this class
 * does a no-op when used with Java 1.3.  The class maintains
 * compatability with Java 1.3 by accessing the java.nio classes
 * using reflection.
 *
 * @author John Lewis
 * @author Mark Doliner
 */
public class FileLocker
{

	/**
	 * An object of type FileLock, created using reflection.
	 */
	private Object lock = null;

	/**
	 * An object of type FileChannel, created using reflection.
	 */
	private Object lockChannel = null;

	/**
	 * A file called "filename.lock" that resides in the same directory
	 * as "filename"
	 */
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
		if (System.getProperty("java.version").startsWith("1.3"))
		{
			return true;
		}

		try
		{
			Class aClass = Class.forName("java.io.RandomAccessFile");
			Method method = aClass.getDeclaredMethod("getChannel", (Class[])null);
			lockChannel = method.invoke(new RandomAccessFile(lockFile, "rw"), (Object[])null);
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Unable to get lock channel for " + lockFile.getAbsolutePath()
					+ ": " + e.getLocalizedMessage());
			return false;
		}
		catch (InvocationTargetException e)
		{
			System.err.println("Unable to get lock channel for " + lockFile.getAbsolutePath()
					+ ": " + e.getLocalizedMessage());
			return false;
		}
		catch (Throwable t)
		{
			System.err.println("Unable to execute RandomAccessFile.getChannel() using reflection: "
					+ t.getLocalizedMessage());
			t.printStackTrace();
		}

		try
		{
			Class aClass = Class.forName("java.nio.channels.FileChannel");
			Method method = aClass.getDeclaredMethod("lock", (Class[])null);
			lock = method.invoke(lockChannel, (Object[])null);
		}
		catch (InvocationTargetException e)
		{
			System.err.println("---------------------------------------");
			e.printStackTrace(System.err);
			System.err.println("---------------------------------------");
			System.err.println("Unable to get lock on " + lockFile.getAbsolutePath() + ": "
					+ e.getLocalizedMessage());
			System.err.println("This is known to happen on Linux kernel 2.6.20.");
			System.err.println("Make sure cobertura.jar is in your server's base classpath.");
			System.err.println("Don't put multiple copies of cobertura.jar in different WEB-INF/lib directories.");
			System.err.println("It is important that cobertura be loaded by the parent classloader.");
			System.err.println("---------------------------------------");
			return false;
		}
		catch (Throwable t)
		{
			System.err.println("Unable to execute FileChannel.lock() using reflection: "
					+ t.getLocalizedMessage());
			t.printStackTrace();
		}

		return true;
	}

	/**
	 * Releases the lock on the file.
	 */
	public void release()
	{
		if (lock != null)
			lock = releaseFileLock(lock);
		if (lockChannel != null)
			lockChannel = closeChannel(lockChannel);
		lockFile.delete();
	}

	private static Object releaseFileLock(Object lock)
	{
		try
		{
			Class aClass = Class.forName("java.nio.channels.FileLock");
			Method method = aClass.getDeclaredMethod("isValid", (Class[])null);
			if (((Boolean)method.invoke(lock, (Object[])null)).booleanValue())
			{
				method = aClass.getDeclaredMethod("release", (Class[])null);
				method.invoke(lock, (Object[])null);
				lock = null;
			}
		}
		catch (Throwable t)
		{
			System.err.println("Unable to release locked file: " + t.getLocalizedMessage());
		}
		return lock;
	}

	private static Object closeChannel(Object channel)
	{
		try
		{
			Class aClass = Class.forName("java.nio.channels.spi.AbstractInterruptibleChannel");
			Method method = aClass.getDeclaredMethod("isOpen", (Class[])null);
			if (((Boolean)method.invoke(channel, (Object[])null)).booleanValue())
			{
				method = aClass.getDeclaredMethod("close", (Class[])null);
				method.invoke(channel, (Object[])null);
				channel = null;
			}
		}
		catch (Throwable t)
		{
			System.err.println("Unable to close file channel: " + t.getLocalizedMessage());
		}
		return channel;
	}

}
