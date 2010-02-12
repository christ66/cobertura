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

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;


public class AtomicCounterMapTest {
	
	@Test
	public void incrementTest(){
		AtomicCounterMap<Integer> map=new AtomicCounterMap<Integer>();
		for(int i=0; i<1000; i++){
			for(int j=i; j>=0; j-- ){
				map.incrementValue(j);				
			}
		}
		Map<Integer,Integer> res=map.getFinalStateAndCleanIt();
		Assert.assertEquals(1000, res.size());
		for(Map.Entry<Integer, Integer> ii:res.entrySet()){
			Assert.assertEquals(1000,ii.getKey()+ii.getValue());
		}
		Assert.assertEquals(0, map.getFinalStateAndCleanIt().size());	
		
		
		for(int i=0; i<100; i++){
			for(int j=i; j>=0; j-- ){
				map.incrementValue(j,2);				
				map.incrementValue(j,-1);
			}
		}		
		res=map.getFinalStateAndCleanIt();
		Assert.assertEquals(100, res.size());
		for(Map.Entry<Integer, Integer> ii:res.entrySet()){
			Assert.assertEquals(100,ii.getKey()+ii.getValue());
		}
	}
	
	//1.347;1.288;1.729;1.287 - counters.putIfAbsent(key, new AtomicInteger(inc)); return ...
	//1.982;1.965;1.965
	//1.935;1.944,1.937 - no return
	//1.923;1.960;1.325;1.951 - final
	@Test
	public void performanceTestInit(){
		AtomicCounterMap<Integer> map=new AtomicCounterMap<Integer>();
		for(int i=0; i<1000000; i++){
			map.incrementValue(i);				
		}
	}
	
	//1.349;1,760;1.363;1.780
	//0.718;0.678;0,681 AtomicInteger v=counters.get(key);if(v!=null){return v.incrementAndGet();}else{v=counters.putIfAbsent(key, new AtomicInteger(1));return (v!=null)?v.incrementAndGet():1;}
	//0.679,0.689,0.681 - no return
	//0.675;0.680,0.710,0.679 - final
	@Test
	public void performanceTestOverride(){
		AtomicCounterMap<Integer> map=new AtomicCounterMap<Integer>();
		for(int i=0; i<10000000; i++){
			map.incrementValue(1);				
		}
	}
	
}
