/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2006 Jiri Mares
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

public class SwitchDataTest extends TestCase
{

	private final SwitchData a = new SwitchData(0, new int[] { 0, 1, 2, 3 });

	private final SwitchData b = new SwitchData(1, 1, 9);

	public void testEquals()
	{
		assertFalse(a.equals(null));
		assertFalse(a.equals(Integer.valueOf(4)));

		assertTrue(a.equals(a));
		assertFalse(a.equals(b));

		SwitchData aPrime = new SwitchData(0, new int[] { 0, 1, 2, 3 });
		assertTrue(a.equals(aPrime));
	}

	public void testHashCode()
	{
		assertEquals(a.hashCode(), a.hashCode());

		SwitchData aPrime = new SwitchData(0, new int[] { 0, 1, 2, 3 });
		assertEquals(a.hashCode(), aPrime.hashCode());
	}

	public void testGetSwitchNumber()
	{
		assertEquals(0, a.getSwitchNumber());
		assertEquals(1, b.getSwitchNumber());
	}

	public void testGetNumbers()
	{
		assertEquals(0, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(0, a.getNumberOfCoveredBranches(), 0);

		for (int i = 0; i < 5; i++)
		{
			a.touchBranch(1);
			assertEquals(0.2, a.getBranchCoverageRate(), 0);
			assertEquals(5, a.getNumberOfValidBranches(), 0);
			assertEquals(1, a.getNumberOfCoveredBranches(), 0);
		}

		a.touchBranch(-1);
		assertEquals(0.4, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(2, a.getNumberOfCoveredBranches(), 0);

		a.touchBranch(0);
		assertEquals(0.6, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(3, a.getNumberOfCoveredBranches(), 0);

		a.touchBranch(2);
		assertEquals(0.8, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(4, a.getNumberOfCoveredBranches(), 0);

		a.touchBranch(3);
		assertEquals(1, a.getBranchCoverageRate(), 0);
		assertEquals(5, a.getNumberOfValidBranches(), 0);
		assertEquals(5, a.getNumberOfCoveredBranches(), 0);
	}

	public void testTouch()
	{
		assertEquals(0, a.getHits(0));
		for (int i = 0; i < 400; i++)
			a.touchBranch(0);
		assertEquals(400, a.getHits(0));

		assertEquals(0, a.getHits(1));
		for (int i = 0; i < 4500; i++)
			a.touchBranch(1);
		assertEquals(4500, a.getHits(1));

		assertEquals(0, a.getHits(2));
		for (int i = 0; i < 300; i++)
			a.touchBranch(2);
		assertEquals(300, a.getHits(2));

		assertEquals(0, a.getHits(3));
		for (int i = 0; i < 800; i++)
			a.touchBranch(3);
		assertEquals(800, a.getHits(3));

		assertEquals(0, a.getDefaultHits());
		for (int i = 0; i < 200; i++)
			a.touchBranch(-1);
		assertEquals(200, a.getDefaultHits());
	}

	public void testMerge()
	{
		a.touchBranch(0);
		a.touchBranch(0);
		a.touchBranch(2);
		a.touchBranch(-1);
		SwitchData x = new SwitchData(0);
		x.touchBranch(3);
		x.touchBranch(3);
		a.merge(x);
		assertEquals(2, a.getHits(0));
		assertEquals(0, a.getHits(1));
		assertEquals(1, a.getHits(2));
		assertEquals(2, a.getHits(3));
		assertEquals(1, a.getDefaultHits());

		x = new SwitchData(0);
		x.touchBranch(5);
		x.touchBranch(-1);
		a.merge(x);
		assertEquals(2, a.getHits(0));
		assertEquals(0, a.getHits(1));
		assertEquals(1, a.getHits(2));
		assertEquals(2, a.getHits(3));
		assertEquals(0, a.getHits(4));
		assertEquals(1, a.getHits(5));
		assertEquals(2, a.getDefaultHits());
	}
	
	private static void touchIteratively(SwitchData data, int num)
	{
		/*
		 * When this test fails, it usually does so well before 2000 iterations.   If it
		 * gets past 2000, it will usually pass, so there is not much need in going much
		 * past 2000.
		 */
		for (int i=0; i<2000; i++)
		{
			/*
			 * The following yield is needed to make sure the other thread gets
			 * some CPU.  Otherwise, this thread will get too much of a jump ahead
			 * of the other thread.
			 */
			Thread.yield(); 
			
			data.touchBranch(i);
		}
	}
	
	private void runTestWithTwoThreads() throws Throwable
	{
		final SwitchData data = new SwitchData(2);
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
				touchIteratively(data, 0);
			}
		};
		Thread thread2 = new Thread(threadGroup, "2") {
			public void run()
			{
				touchIteratively(data, 1);
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
	}

	public void testMultiThreaded() throws Throwable
	{
		/*
		 * This test will often pass with only one iteration.
		 * It passes once in a while with 4.   It never passes
		 * with 10 (I hope).
		 */
		for (int i=0; i<10; i++)
		{
			runTestWithTwoThreads();
		}
	}
}
