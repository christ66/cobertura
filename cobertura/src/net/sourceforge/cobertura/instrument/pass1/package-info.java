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

/**
 * There are cases when the same piece of java code generates multiple series of ASM instructions. 
 * In particular the same line of code can be rendered in many places of destination code. See 
 * {@link DetectDuplicatedCodeClassVisitor} for example. 
 * 
 * This package contains {@link DetectDuplicatedCodeClassVisitor} that is responsible for
 * detecting such a duplicates. It does not change class but only provide analysis. 
 */
package net.sourceforge.cobertura.instrument.pass1;