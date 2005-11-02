/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Eugene Kuleshov
 * Copyright (C) 2005 Grzegorz Lukasik
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

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

/**
 * Provides support for custom class file attributes.
 * 
 * <p>
 * This class handles attributes that can be found in class files
 * and are not handled by default by ASM. ASM 2.1 supports only
 * standard attributes defined in JVM specification, are other 
 * attributes are marked as unknown.
 * </p>
 * 
 * <p>
 * This class allows custom attributes to be passed to instrumented file. 
 * All custom attributes are currently just passed from source to instrumented
 * file, no interpretation or parsing of data contained in attributes is done.
 * </p>
 * 
 * <p>
 * For now AspectJ attributes are handled.
 * </p>
 * 
 */
public class CustomAttribute extends Attribute {
	// Contains prototypes for attributes used by AspectJ
	private static Attribute[] extraAttributes = {
			new CustomAttribute("org.aspectj.weaver.AjSynthetic"),
			new CustomAttribute("org.aspectj.weaver.TypeMunger"),
			new CustomAttribute("org.aspectj.weaver.WeaverState"),
			new CustomAttribute("org.aspectj.weaver.WeaverVersion"),
			new CustomAttribute("org.aspectj.weaver.SourceContext"),
			new CustomAttribute("org.aspectj.weaver.MethodDeclarationLineNumber"),
			new CustomAttribute("org.aspectj.weaver.PointcutDeclaration"),
			new CustomAttribute("org.aspectj.weaver.Declare"),
			new CustomAttribute("org.aspectj.weaver.Advice"),
			new CustomAttribute("org.aspectj.weaver.Aspect"),
			new CustomAttribute("org.aspectj.weaver.Privileged"),
			new CustomAttribute("org.aspectj.weaver.EffectiveSignature") };

	// Bytes that define single attribute
	private byte[] data;

	/**
	 * Creates a new prototype attribute with no data. Prototype means that
	 * an instance of this class will be used to create other attributes
	 * of the same type - check {@link #read(ClassReader, int, int, char[], int, Label[])},
	 * and itself this instance do not map to real attribute.
	 * 
	 * @param type A type of attribute as found in class file
	 */ 
	protected CustomAttribute(String type) {
		super(type);
	}

	/**
	 * Creates a new attribute with the specified type and data. One instance of
	 * this class maps to single attribute found in class file. It can be any
	 * kind of attribute - class, method, field or code.
	 * 
	 * @param type A type of attribute as found in class file
	 * @param data Bytes that define this attribute
	 */
	public CustomAttribute(String type, byte[] data) {
		super(type);
		this.data = data;
	}

	/**
	 * <p>
	 * Creates a new attribute from bytes stored in class file. Returned 
	 * attribute is of the same type as this attribute but maps to real
	 * attribute. An instance on which this method is called can be treaten 
	 * as prototype attribute that does not map to any real attribute but 
	 * is used to create new instances.
	 * </p>
	 * 
	 * <p>
	 * This implementation just stores bytes passed in the method - no parsing
	 * of the data is made.
	 * </p>
	 * 
	 * @see Attribute#read(ClassReader, int, int, char[], int, Label[])
	 */
	protected Attribute read(ClassReader cr, int off, int len, char[] buf,
			int codeOff, Label[] labels) {
		byte[] data = new byte[len];
		System.arraycopy(cr.b, off, data, 0, len);
		return new CustomAttribute(this.type, data);
	}

	/**
	 * <p>
	 * Returns serialized version of this attribute. This implementation
	 * returns bytes passed in construction of this attribute untouched.
	 * </p>
	 * 
	 * @see Attribute#write(ClassWriter, byte[], int, int, int)
	 */
	protected ByteVector write(ClassWriter cw, byte[] code, int len,
			int maxStack, int maxLocals) {
		return new ByteVector().putByteArray(data, 0, data.length);
	}
	
	/**
	 * Returns true if this attribute is unknown. All custom attributes 
	 * are known for us - we just pass them unchanged to result file.
	 *  
	 * @returns false
	 */
	public boolean isUnknown() {
		return false;
	}

	/**
	 * Returns an array with all supported custom attributes. All attributes 
	 * from this array will be treaten as known by ASM and will be stored
	 * inside instrumented file.
	 * 
	 * @return An array with all supported custom attributes
	 */
	public static Attribute[] getExtraAttributes() {
		return extraAttributes;
	}
}
