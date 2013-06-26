/**
 * 
 */
package net.sourceforge.cobertura.coveragedata;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Move the data locking algorithms to this new api.
 * 
 * @author schristou88
 *
 */
public class CoverageDataLock implements Serializable {
	protected transient Lock lock;
	
	protected void initLock() {
		lock = new ReentrantLock();
	}
	
	protected void getBothLocks(CoverageDataLock other) {
		/*
		 * To prevent deadlock, we need to get both locks or none at all.
		 * 
		 * When this method returns, the thread will have both locks.
		 * Make sure you unlock them!
		 */
		boolean myLock = false;
		boolean otherLock = false;
		while ((!myLock) || (!otherLock)) {
			try {
				myLock = lock.tryLock();
				otherLock = other.lock.tryLock();
			} finally {
				if ((!myLock) || (!otherLock)) {
					//could not obtain both locks - so unlock the one we got.
					if (myLock) {
						lock.unlock();
					}
					if (otherLock) {
						other.lock.unlock();
					}
					//do a yield so the other threads will get to work.
					Thread.yield();
				}
			}
		}
	}
}
