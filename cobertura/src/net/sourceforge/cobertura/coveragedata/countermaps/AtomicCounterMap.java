/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2010 Piotr Tabor
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

package net.sourceforge.cobertura.coveragedata.countermaps;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.cobertura.coveragedata.HasBeenInstrumented;

/**
 * Thread-safe implementation of map that counts number of keys (like multi-set)
 * @author ptab
 *
 * @param <T>
 */
public class AtomicCounterMap<T> implements CounterMap<T>,HasBeenInstrumented{
	private final ConcurrentMap<T, AtomicInteger> counters=new ConcurrentHashMap<T, AtomicInteger>();
	
	public final void incrementValue(T key, int inc){
		AtomicInteger v=counters.get(key);
		if(v!=null){
			v.addAndGet(inc);
		}else{
			v=counters.putIfAbsent(key, new AtomicInteger(inc));
			if(v!=null)v.addAndGet(inc);			
		}
	}
	
	public final void incrementValue(T key){
		//AtomicInteger v=counters.putIfAbsent(key, new AtomicInteger(1));
		//return (v!=null)?v.incrementAndGet():1;
		AtomicInteger v=counters.get(key);
		if(v!=null){
			v.incrementAndGet();			
		}else{
			v=counters.putIfAbsent(key, new AtomicInteger(1));
			if(v!=null)v.incrementAndGet();
		}
	}	
	
	public final int getValue(T key){
		AtomicInteger v=counters.get(key);
		return v==null?0:v.get();
	}
	
	
	public synchronized  Map<T,Integer> getFinalStateAndCleanIt(){		
		Map<T,Integer> res=new LinkedHashMap<T, Integer>();
		Iterator<Map.Entry<T, AtomicInteger>> iterator=counters.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<T, AtomicInteger> entry=iterator.next();
			T key=entry.getKey();
			int old=entry.getValue().get();
			iterator.remove();
			if(old>0){
				res.put(key, old);
			}
		}		
		return res;		
	}
	
	public int getSize(){
		return counters.size();
	}
}
