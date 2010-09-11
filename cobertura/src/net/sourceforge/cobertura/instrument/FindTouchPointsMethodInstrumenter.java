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

package net.sourceforge.cobertura.instrument;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import net.sourceforge.cobertura.util.RegexUtil;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Analyzes given method, assign unique event identifiers to every found
 * interesting instruction and calls business method in the {@link #touchPointListener}. 
 * 
 * @author ptab
 */
class FindTouchPointsMethodInstrumenter extends ContextMethodAwareMethodAdapter {
	/**
	 * Source of identifiers for events.
	 * 
	 *  <p>Remember to acquire identifiers using {@link AtomicInteger#incrementAndGet()} (not {@link AtomicInteger#getAndIncrement()}!!!)</p>
	 */
	private final AtomicInteger eventIdGenerator;

	/**
	 * Backing listener that will be informed about all interesting events found
	 */
	private TouchPointListener touchPointListener;
	
	/**
	 * Line number of current line. 
	 * 
	 *  <p>It it NOT lineId</pl>
	 */
	private int currentLine;
	
	/**
	 * List of patterns to know that we don't want trace lines that are calls to some methods
	 */
	private Collection<Pattern> ignoreRegexp;

	/**
	 * See {@link AbstractFindTouchPointsClassInstrumenter#duplicatedLinesMap}  
	 */
	private final Map<Integer, Map<Integer, Integer>> duplicatedLinesMap;

	/**
	 * Map of (line number -> (lineId -> List of eventIds)). 
	 * 
	 * <p>For every line number, and for evere lineId in the line we store list of all generated events</p>
	 * 
	 * <p>When we will detect duplicated block of code in given line - instead of generating new eventIds we will
	 * use the same events</p> 
	 */
	private final Map<Integer, Map<Integer, LinkedList<Integer>>> line2eventIds = new HashMap<Integer, Map<Integer, LinkedList<Integer>>>();
	
	/**
	 * If we are currently processing a new (not duplicated line), it is a list (linked into {@link #line2eventIds}) that we use to store newly generated identifiers into it,
	 * otherwise it  is null.
	 */
	private LinkedList<Integer> saveEventIdList = null;

	/**
	 * If we are currently processing a duplicated line, it is a list of identifiers that should be used for the line. After processing event, you should remove identifier from the begining of the list.  
	 */
	private LinkedList<Integer> replyEventIdList = null;

	public FindTouchPointsMethodInstrumenter(MethodVisitor arg0,
			String className, String methodName, String methodSignature,
			AtomicInteger eventIdGenerator,
			Map<Integer, Map<Integer, Integer>> duplicatedLinesMap,
			AtomicInteger lineIdGenerator) {
		super(arg0, className, methodName, methodSignature, lineIdGenerator);
		this.eventIdGenerator = eventIdGenerator;
		this.duplicatedLinesMap = duplicatedLinesMap;
	}

	private int generateNewEventId() {
		return eventIdGenerator.incrementAndGet();
	}

	/**
	 * Depending on situation if we are processing a new line or duplicated line, 
	 * generates a new identifier or reuses previously generated for the same event.
	 * @return
	 */
	private int getEventId() {
		if (replyEventIdList != null && !replyEventIdList.isEmpty()) {
			//in case of a duplicated line
			return replyEventIdList.removeFirst();
		} else {
			// in case of a new line
			int eventId = generateNewEventId();
			if (saveEventIdList != null) {
				saveEventIdList.addLast(eventId);
			}
			return eventId;
		}
	}

	@Override
	public void visitCode() {
		super.visitCode();
		touchPointListener.afterMethodStart(mv);
	}

	/**
	 * Processing information about new line. 
	 * 
	 * Upgrades {@link #replyEventIdList} and {@link #saveEventIdList} and calls {@link TouchPointListener#afterLineNumber(int, Label, int, MethodVisitor, String, String)}
	 */
	public void visitLineNumber(int line, Label label) {
		super.visitLineNumber(line, label);
		currentLine = line;

		if (!isDuplicatedLine(line, lastLineId)) {
			/*
			 * It is a new line (first time seen, so we will save all found
			 * events)
			 */
			replyEventIdList = null;
			saveEventIdList = new LinkedList<Integer>();
			Map<Integer, LinkedList<Integer>> eventsMap = line2eventIds
					.get(line);
			if (eventsMap == null) {
				eventsMap = new HashMap<Integer, LinkedList<Integer>>();
				line2eventIds.put(line, eventsMap);
			}
			eventsMap.put(lastLineId, saveEventIdList);
		} else {
			Integer orgin = getOriginForLine(line, lastLineId);
			Map<Integer, LinkedList<Integer>> m = line2eventIds	.get(currentLine);
			LinkedList<Integer> eventIds = m.get(orgin);
			
			/* copy of  current list */
			replyEventIdList = new LinkedList<Integer>(eventIds);
			saveEventIdList = null;
		}

		touchPointListener.afterLineNumber(getEventId(), label, currentLine,
				mv, methodName, methodSignature);
	}

	/**
	 * Checks if given line is a duplicate of previously processed line
	 * 
	 * @param line - line number
	 * @param lineId - line identifier
	 * @return true - if line is duplicate of previously processed lines
	 */
	private boolean isDuplicatedLine(int line, Integer lineId) {
		return getOriginForLine(line, lineId) != null;
	}
	
	/** 
	 * @param line - line number
	 * @param lineId - line identifier
	 * 
	 * @return lineId of the origin line (first processed line) that the given line is duplicate of. 
	 *    If the current line is not an duplicate of the previously processed line the method returns NULL.  
	 */
	private Integer getOriginForLine(int line, Integer lineId) {
		Map<Integer, Integer> labelMap = duplicatedLinesMap.get(line);
		return labelMap != null ? labelMap.get(lineId) : null;
	}

	@Override
	public void visitLabel(Label label) {
		int eventId = getEventId();
		touchPointListener.beforeLabel(eventId, label, currentLine, mv);
		super.visitLabel(label);
		touchPointListener.afterLabel(eventId, label, currentLine, mv);
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		/* Ignore any jump instructions in the "class init" method.
		 When initializing static variables, the JVM first checks
		 that the variable is null before attempting to set it.
		 This check contains an IFNONNULL jump instruction which
		 would confuse people if it showed up in the reports.*/
		if ((opcode != Opcodes.GOTO) && (opcode != Opcodes.GOTO)
				&& (opcode != Opcodes.JSR) && (currentLine != 0)
				&& (!methodName.equals("<clinit>"))) {
			int eventId = getEventId();
			touchPointListener.beforeJump(eventId, label, currentLine, mv);
			super.visitJumpInsn(opcode, label);
			touchPointListener.afterJump(eventId, label, currentLine, mv);
		} else {
			super.visitJumpInsn(opcode, label);
		}
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String method,
			String descr) {
		super.visitMethodInsn(opcode, owner, method, descr);
		//We skip lines that contains call to methods that are specified inside ignoreRegexp 
		if (RegexUtil.matches(ignoreRegexp, owner)) {
			touchPointListener.ignoreLine(getEventId(), currentLine);
		}
	}

	@Override
	public void visitLookupSwitchInsn(Label def, int[] values, Label[] labels) {
		touchPointListener.beforeSwitch(getEventId(), def, labels, currentLine,
				mv);
		super.visitLookupSwitchInsn(def, values, labels);
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label def, Label[] labels) {
		touchPointListener.beforeSwitch(getEventId(), def, labels, currentLine,
				mv);
		super.visitTableSwitchInsn(min, max, def, labels);
	}

	// ===========  Getters and setters =====================
	
	/**
	 * Gets backing listener that will be informed about all interesting events found
	 */
	public TouchPointListener getTouchPointListener() {
		return touchPointListener;
	}

	/**
	 * Sets backing listener that will be informed about all interesting events found
	 */
	public void setTouchPointListener(TouchPointListener touchPointListener) {
		this.touchPointListener = touchPointListener;
	}

	/**
	 * @return list of patterns to know that we don't want trace lines that are calls to some methods
	 */
	public Collection<Pattern> getIgnoreRegexp() {
		return ignoreRegexp;
	}

	/**
	 * sets list of patterns to know that we don't want trace lines that are calls to some methods
	 */
	public void setIgnoreRegexp(Collection<Pattern> ignoreRegexp) {
		this.ignoreRegexp = ignoreRegexp;
	}

}