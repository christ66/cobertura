/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * This file was taken from JavaNCSS
 * http://www.kclee.com/clemens/java/javancss/
 * Copyright (C) 2000 Chr. Clemens Lee <clemens a.t kclee d.o.t com>
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


/*
 *
 * WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING  
 *
 * WARNING TO COBERTURA DEVELOPERS
 *
 * DO NOT MODIFY THIS FILE!
 *
 * MODIFY THE FILES UNDER THE JAVANCSS DIRECTORY LOCATED AT THE ROOT OF THE COBERTURA PROJECT.
 *
 * FOLLOW THE PROCEDURE FOR MERGING THE LATEST JAVANCSS INTO COBERTURA LOCATED AT
 * javancss/coberturaREADME.txt
 *
 * WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   WARNING   
 */
package net.sourceforge.cobertura.javancss;

/**
 * Contains a couple of constants used to fill and access data
 * structures containing Java metric data.
 *
 * @author    Chr. Clemens Lee <clemens@kclee.com>
 * $Id: JavancssConstants.java 15 2008-08-04 22:00:07Z hboutemy $
 */
public interface JavancssConstants
{
  static final int FCT_NAME = 0;
  static final int FCT_NCSS = 1;
  static final int FCT_CCN  = 2;
  static final int FCT_JVDC = 3;

  // added by SMS
  static final int FCT_JVDC_LINES   = 4;
  static final int FCT_SINGLE_LINES = 5;
  static final int FCT_MULTI_LINES  = 6;

  // specially added for Cobertura
  // Commenting out for now until the rest of patch 2353196 is applied.
  //static final int FCT_BEGIN_LINE_NUMBER  = 7;
  // specially added for Cobertura
  // Commenting out for now until the rest of patch 2353196 is applied.
  //static final int FCT_END_LINE_NUMBER  = 8;
  
  static final int OBJ_NAME  = 0;
  static final int OBJ_NCSS  = 1;
  static final int OBJ_FCTS  = 2;
  static final int OBJ_CLSSS = 3;
  //static final int OBJ_JVDCS = 4;
  //static final int OBJ_JVDCS = 5;
  static final int OBJ_JVDCS = 6;

  // added by SMS
  static final int OBJ_JVDC_LINES   = 7;
  static final int OBJ_SINGLE_LINES = 8;
  static final int OBJ_MULTI_LINES  = 9;
}
