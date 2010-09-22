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

package net.sourceforge.cobertura.instrument.pass3;

import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.instrument.tp.ClassMap;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Universal API for all methods that are responsible for generating any JASM code that have
 * to be injected into real classes. 
 * 
 *  The general idea is that injected code is responsible for incrementing counters. The realization of counters
 *  is implementation dependent. 
 * 
 * @author piotr.tabor@gmail.com
 *
 */
public interface CodeProvider {
	/**
	 * Generates fields injected into  instrumented class  by cobertura. 
	 * 
	 * @param cv - ClassVisitor that is listener of code-generation events
	 */
	public abstract void generateCountersField(ClassVisitor cv);
	
	/**
	 * Generates code that is injected into static constructor of an instrumented class.  
	 * 
	 * It is good place to initiate static fields inserted into a class ({@link #generateCountersField(ClassVisitor)}), 
	 * or execute other code that should be executed when the class it used for the first time. Registering the class in 
	 * {@link TouchCollector} would be a bright idea.
	 * 
	 * It is expected that all counter will be set to zero after that operation. 	   
	 * 
	 * @param mv           - {@link MethodVisitor} that is listener of code-generation events 
	 * @param className    - internal name (asm) of class being instrumented  
	 * @param counters_cnt - information about how many counters are expected to be used by instrumentation code. 
	 *                       In most cases the method is responsible for allocating objects that will be used to store counters.  
	 */
	public abstract void generateCINITmethod(MethodVisitor mv, String className, int counters_cnt);

	/**
	 * Injects code that increments counter given by parameter.     
	 * 
	 * @param nextMethodVisitor - {@link MethodVisitor} that is listener of code-generation events
	 * @param counterId -  counterId of counter that have to be incremented 
	 * @param className  - internal name (asm) of class being instrumented
	 */	
	public abstract void generateCodeThatIncrementsCoberturaCounter(
			MethodVisitor nextMethodVisitor, Integer counterId, String className);
	
	/**
	 * Injects code that increments counter given by internal variable. 
	 * The id of the variable is identified by lastJumpIdVariableIndex. The variable is in most cases set (by {@link #generateCodeThatSetsJumpCounterIdVariable(MethodVisitor, int, int)} 
	 * to some counterId and in the target label, the counter identified by the variable is incremented.    
	 * 
	 * @param nextMethodVisitor          - {@link MethodVisitor} that is listener of code-generation events
	 * @param lastJumpIdVariableIndex    - id of the variable used to store counterId that have to be incremented 
	 * @param className                  - internal name (asm) of class being instrumented
	 */	
	public abstract void generateCodeThatIncrementsCoberturaCounterFromInternalVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex,
			String className);

	/**
	 * Injects code that sets internal variable (identified by lastJumpIdVariableIndex) to given value.
	 * 
	 * @param nextMethodVisitor       - {@link MethodVisitor} that is listener of code-generation events
	 * @param new_value               - value to set the variable to 
	 * @param lastJumpIdVariableIndex - index of variable that have to be set
	 */
	public abstract void generateCodeThatSetsJumpCounterIdVariable(
			MethodVisitor nextMethodVisitor, int new_value,
			int lastJumpIdVariableIndex);

	/**
	 * Injects code that sets internal variable (identified by lastJumpIdVariableIndex) to zero.
	 * 
	 * @param nextMethodVisitor       - {@link MethodVisitor} that is listener of code-generation events 
	 * @param lastJumpIdVariableIndex - index of variable that have to be set
	 */
	public abstract void generateCodeThatZeroJumpCounterIdVariable(
			MethodVisitor nextMethodVisitor, int lastJumpIdVariableIndex);

	/**
	 * Injects code that behaves the same as such a code snippet:  
	 * <pre>
	 * if (value('lastJumpIdVariableIndex')==neededJumpCounterIdVariableValue){
	 * 	 cobertura_counters.increment(counterIdToIncrement);
	 * }
	 * </pre>
	 * 
	 * This snippet is used in switch case of switch statement. We have a label and we want to ensure that
	 * we are executing the label in effect of switch statement-jump, and not other JUMP or fall-throught.  
	 */
	public abstract void generateCodeThatIncrementsCoberturaCounterIfVariableEqualsAndCleanVariable(
			MethodVisitor nextMethodVisitor,
			Integer neededJumpCounterIdVariableValue,
			Integer counterIdToIncrement, int lastJumpIdVariableIndex,
			String className);

	/**
	 * The version of cobertura prior to 1.10 used *.ser file to store information of lines, jumps, switches and other
	 * constructions used in the class. It was difficult to user to transfer the files after instrumentation into 
	 * 'production' directory. To avoid that we are now creating the class-map as a special injected method that is responsible
	 * for keeping such a informations. 	 *     
	 * 
	 * @param cv - listener used to incject the code
	 * @param classMap - structure that is keeping all collected information about the class. The information from the structure will be stored as
	 * 					 method body. 
	 */
	public void generateCoberturaClassMapMethod(ClassVisitor cv,ClassMap classMap);
}