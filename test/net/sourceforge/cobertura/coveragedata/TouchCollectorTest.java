/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2010 John Lewis
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

package net.sourceforge.cobertura.coveragedata;

import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

public class TouchCollectorTest extends TestCase
{
	private static void touchIteratively(int num)
	{
		for (int i=0; i<2000; i++)
		{
			/*
			 * The following yield is needed to make sure the other thread gets
			 * some CPU.  Otherwise, this thread will get too much of a jump ahead
			 * of the other thread.
			 */
			Thread.yield(); 
			
			TouchCollector.touch(Integer.toString(i),1);
		}
	}

	private void runTestWithTwoThreads() throws Throwable
	{
		final AtomicReference<Throwable> possibleThrowable = new AtomicReference<Throwable>();

		ThreadGroup threadGroup = new ThreadGroup("TestThreadGroup") {
			public void uncaughtException(Thread thread, Throwable t)
			{
				/*
				 * Save the Throwable for later use and interrupt this thread so it exits
				 */
				possibleThrowable.set(t);
				thread.interrupt();
			}
		};

		/*
		 * Create two threads using the above thread group
		 */
		Thread thread1 = new Thread(threadGroup, "1") {
			public void run()
			{
				touchIteratively(0);
			}
		};
		Thread thread2 = new Thread(threadGroup, "2") {
			public void run()
			{
				touchIteratively(1);
			}
		};
		thread1.start();
		thread2.start();
		/*
		 * Wait for the threads to exit
		 */
		if (thread1.isAlive()) thread1.join();
		if (thread2.isAlive()) thread2.join();
		Throwable t = possibleThrowable.get();
		if (t != null)
		{
			throw t;
		}
		TouchCollector.applyTouchesOnProjectData(new ProjectData());
	}

	/**
	 * Tests the thread safety of the TouchCollector.   Since TouchCollector has all
	 * static methods, it is difficult to get this test to fail everytime if
	 * there is a thread problem.
	 * 
	 * At the time this test was written, TouchCollector had a problem, but
	 * this test needed to be run a few times before seeing a failure.   The
	 * majority of times it would fail though.
	 */
	public void testMultiThreaded() throws Throwable
	{
		runTestWithTwoThreads();
	}

}
