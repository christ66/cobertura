/*
 * Cobertura - http://cobertura.sourceforge.net/
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


package net.sourceforge.cobertura.instrument.tp;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.cobertura.coveragedata.LineData;

import org.objectweb.asm.Label;

/**
 * Class representing a touch-point connected to a a SWITCH instruction in a source-code
 * 
 * <p>A SWITCH touch-point uses one more counter then distinct number destination labels ({@link #getCountersForLabelsCnt()}).<br/>
 * 	 One 'internal' counterId ({@link #counterId}) is a special identifier of SWITCH statement (used in runtime), but in fact we don't expect any
 * incrementation of the counter. We implemented this to use a counterId because we are storing the value inside 'internal variable' and we need to be sure
 * that the value is connected to the last seen SWITCH statement.<br/>
 * 
 * Or other counterIds represents different branches (different destination labels of the switch). 
 * </p> 
 * 
 * <p>We also storing a {@link #methodName} and a {@link #methodSignature} (consider to move this fields into {@link TouchPointDescriptor}).
 * Those fields are needed to properly create instance of {@link LineData}. </p> 
 *   
 * @author piotr.tabor@gmail.com
 */
public class SwitchTouchPointDescriptor extends TouchPointDescriptor{	
	private Label defaultDestinationLabel;
	private Label[] labels;
	private Integer counterId;
	private Map<Label,Integer> label2counterId;

	/**
	 * Creates o new switch-touch point. 
	 * @param eventId     - eventId connected to the SWITCH instruction
	 * @param currentLine - line number of the switch
	 * @param def	      - internal identifier of a default destination label
	 * @param labels      - table of other destination labels for different values (duplicates allowed) 
	 */
	public SwitchTouchPointDescriptor(int eventId, int currentLine, Label def,	Label[] labels) {
		super(eventId, currentLine);
		this.labels=labels;
		this.defaultDestinationLabel=def;
	}

	public Label getDefaultDestinationLabel() {
		return defaultDestinationLabel;
	}
	public void setDefaultDestinationLabel(Label defaultDestinationLabel) {
		this.defaultDestinationLabel = defaultDestinationLabel;
	}
	public Label[] getLabels() {
		return labels;
	}
	public void setLabels(Label[] labels) {
		this.labels = labels;
	}
	
	public Integer getCounterId() {
		return counterId;
	}
	public void setCounterId(Integer counterId) {
		this.counterId = counterId;
	}
	
	@Override
	public int assignCounters(AtomicInteger idGenerator) {
		counterId=idGenerator.incrementAndGet();
		label2counterId=new HashMap<Label, Integer>();
		int idp=idGenerator.incrementAndGet();
		label2counterId.put(defaultDestinationLabel,idp);
		int i=0; 
		for(Label l:labels){
			i++;
			idp=idGenerator.incrementAndGet();
			label2counterId.put(l, idp);
		}
		return i+2;
	}


	public Integer getCounterIdForLabel(Label label) {
		return label2counterId.get(label);
	}
	
	public Collection<Integer> getCountersForLabels(){
		return label2counterId.values();
	}
	
	/**
	 * <p>Works before calling 'assignCounters'</p>
	 * 
	 * @return Number of distinct destination labels of the SWITCH (It's the same as number of branches supported by the switch).
	 *  
	 */
	public int getCountersForLabelsCnt(){
		Set<Label> l=new HashSet<Label>(Arrays.asList(labels));
		l.add(defaultDestinationLabel);
		return l.size();
	}
	
}
