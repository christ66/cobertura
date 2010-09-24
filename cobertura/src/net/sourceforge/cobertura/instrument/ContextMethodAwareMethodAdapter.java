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

import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * Abstract implementation of {@link MethodAdapter} that:
 * <ul>
 *    <li>provides information about {@link #className},{@link #methodName} and {@link #methodSignature} of method currently being instrumented/analyzed</li>
 *    <li>Assign line identifiers (see {@link AbstractFindTouchPointsClassInstrumenter#lineIdGenerator} to every LINENUMBER asm instruction found</li> 
 * </ul>
 * 
 * @author ptab
 *
 */
public abstract class ContextMethodAwareMethodAdapter extends MethodAdapter{
    protected final String className;
	protected final String methodName;
	protected final String methodSignature;
	
	/**
	 * What was the last lineId assigned. We can read this field to know which line (by identifier) we are currently analyzing 
	 */
	protected int lastLineId;
	
	/**
	 * Generator that assigns unique (in scope of single class) identifiers to every LINENUMBER asm derective. 
	 * 
	 * <p>We will use this 'generator' to provide this identifiers. Remember to acquire identifiers using {@link AtomicInteger#incrementAndGet()} (not {@link AtomicInteger#getAndIncrement()}!!!)</p> 
	 */
	protected final AtomicInteger lineIdGenerator; 
	
	public ContextMethodAwareMethodAdapter(MethodVisitor mv, String className, String methodName, String methodSignature,AtomicInteger lineIdGenerator) {
		super(mv);
		this.className=className;
		this.methodName=methodName;
		this.methodSignature=methodSignature;
		lastLineId=0;
		this.lineIdGenerator=lineIdGenerator;
	}
	
	@Override
	public void visitLineNumber(int number, Label label) {	
		lastLineId=lineIdGenerator.incrementAndGet();
		super.visitLineNumber(number, label);		
	}

}
