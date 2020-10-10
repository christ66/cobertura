/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the reusable ccl java library
 * (http://www.kclee.com/clemens/java/ccl/).
 *
 * The Initial Developer of the Original Code is
 * Chr. Clemens Lee.
 * Portions created by Chr. Clemens Lee are Copyright (C) 2002
 * Chr. Clemens Lee. All Rights Reserved.
 *
 * Contributor(s): Chr. Clemens Lee <clemens@kclee.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package net.sourceforge.cobertura.javancss.ccl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A general purpose class with a variety of support and convenience methods.
 *
 * <p> There are different groups of methods in this class:
 * <br>
 * <br><a href="#setDebug(boolean)">debug and assertion methods</a>
 * <br><a href="#print(char)">print methods</a> - convenience methods for System.out.print etc. that additionally make sure output is gets flushed immediately.
 * <br><a href="#atoi(java.lang.String)">basic converter methods</a>
 * <br><a href="#isEmpty(java.lang.String)">string methods</a>
 * <br><a href="#concat(java.util.Vector)">string/vector converter methods</a>
 * <br><a href="#toVector(java.util.Enumeration)">vector methods</a>
 * <br><a href="#quickSort(java.lang.Object[], int, int, ccl.util.Comparable)">sorting and inserting methods</a>
 * <br><a href="#system(java.lang.String)">system methods</a>
 * <br><a href="#getStandardDate(java.util.Date)">date methods</a>
 * <br><a href="#rnd()">random generator methods</a>
 * <br><a href="#getConstantObject()">miscellaneous methods</a>
 * <p>
 * <p>
 * Some basic but none the less the most used methods by myself are:<br>
 * - {@link #println(java.lang.String) println}<br>
 * - {@link #isEmpty(java.lang.String) isEmpty}<br>
 * - {@link #stringToLines(java.lang.String) stringToLines}<br>
 * - {@link #getDate() getDate}
 * <p>
 * <p>
 * Potential future but not yet existing classes to move some code into
 * are:<br>
 * StringUtil, VectorUtil, SystemUtil, and maybe Debug.
 *
 * @author <a href="http://www.kclee.com/clemens/">
 * Chr. Clemens Lee</a>
 * &lt;<a href="mailto:clemens@kclee.com">
 * clemens@kclee.com
 * </a>>
 * @version $Id: Util.java 1.87 2003/11/23 09:13:55 clemens Exp clemens $
 */
public class Util {
    // -----------------------------------------------------
    // attributes
    // -----------------------------------------------------

    /**
     * Used to store the debug state. Can be changed during
     * runtime for more flexibility. This is more important
     * to me than speed.
     */
    private static boolean _bDebug = false;

    // -----------------------------------------------------
    // constructor
    // -----------------------------------------------------

    /**
     * This is an utility class, there is (should be) no need
     * for an instance of this class.
     */
    private Util() {
        super();
    }


    // -----------------------------------------------------
    // debug methods and assertion stuff
    // -----------------------------------------------------

    /**
     * Sets the debug mode for the running application. When
     * true, all following debug statements are equal to println
     * statements, otherwise they are ignored.
     *
     * @see #debug(Object)
     */
    public static void setDebug(boolean bDebug_) {
        _bDebug = bDebug_;
    }

    /**
     * Returns the current debug mode.
     *
     * @return the current debug mode.
     */
    public static boolean isDebug() {
        return _bDebug;
    }

    /**
     * Sets the debug mode for the running application. When
     * true, all following debug statements are equal to
     * printlnErr statements, otherwise they are ignored.
     *
     * @see #debug(Object)
     */
    public static void setDebug(String sDebug_) {
        setDebug(atob(sDebug_));
    }

    /**
     * If the debug mode was set with the setDebug function
     * to true, this debug statements is equal to a printlnErr
     * statement, otherwise it will be ignored.
     *
     * @see #setDebug(boolean)
     */
    public static void debug(Object oMessage_) {
        if (_bDebug) {
            printlnErr(oMessage_.toString());
        }
    }

    /**
     * If the debug mode was set with the setDebug function
     * to true, this debug statements is equal to a printlnErr
     * statement, otherwise it will be ignored.
     *
     * @see #setDebug(boolean)
     */
    public static void debug(int i) {
        debug("int: " + i);
    }

    /**
     * If the debug mode was set with the setDebug function
     * to true, this debug statements is equal to a printlnErr
     * statement, otherwise it will be ignored.
     *
     * @see #setDebug(boolean)
     * @see #printlnErr(java.lang.Object, java.lang.Object)
     */
    public static void debug(Object oOriginator_,
                             Object oMessage_) {
        if (_bDebug) {
            printlnErr(oOriginator_, oMessage_);
        }
    }

    /**
     * If the debug mode was set with the setDebug function
     * to true, this debug statements is equal to a printlnErr
     * statement, otherwise it will be ignored.
     *
     * @see #setDebug(boolean)
     * @see #printlnErr(java.lang.Object, java.lang.Object)
     */
    public static void debug(Class<?> cOriginator_,
                             Object oMessage_) {
        if (_bDebug) {
            printlnErr(cOriginator_, oMessage_);
        }
    }

    // -----------------------------------------------------
    // print methods
    // -----------------------------------------------------

    /**
     * Prints out a char to System.out.
     * Unlike using System.out directly this
     * method makes sure the content gets flushed out immediately.
     */
    public static void print(char c_) {
        System.out.print(c_);
        System.out.flush();
    }

    /**
     * Prints out a String to System.out.
     * Unlike using System.out directly this
     * method makes sure the content gets flushed out immediately.
     */
    public static void print(String pString_) {
        System.out.print(pString_);
        System.out.flush();
    }

    /**
     * Prints out the object to System.out.
     * Unlike using System.out directly this
     * method makes sure the content gets flushed out immediately.
     */
    public static void print(Object pObject_) {
        if (pObject_ == null) {
            print("null");
        } else {
            print(pObject_.toString());
        }
    }

    /**
     * Prints out a String to System.out together with a
     * new line.
     * Unlike using System.out directly this
     * method makes sure the content gets flushed out immediately.
     *
     * @param pString_ a string without a trailing newline to send
     *                 to standard output.
     */
    public static void println(String pString_) {
        System.out.println(pString_);
        System.out.flush();
    }

    /**
     * Prints out the exception, its stack trace,
     * and the current thread to System.out!
     * Unlike using System.out directly this
     * method makes sure the content gets flushed out immediately.
     */
    public static void println(Exception e) {
        System.out.println("Exception: " + e.getMessage());
        /*//e.fillInStackTrace();
          //e.printStackTrace(System.out);*/
        Thread.dumpStack();
        println(Thread.currentThread().toString());
    }

    /**
     * Prints out the object to System.out together with a
     * new line.
     * Unlike using System.out directly this
     * method makes sure the content gets flushed out immediately.
     */
    public static void println(Object pObject_) {
        print(pObject_);
        print('\n');
    }

    /**
     * Same as print('\n').
     *
     * @see #print(char) print
     */
    public static void println() {
        print('\n');
    }

    /**
     * Prints out a char to System.err.
     */
    public static void printErr(char c_) {
        System.err.print(c_);
        System.err.flush();
    }

    /**
     * Prints out a String to System.err.
     */
    public static void printErr(String pString_) {
        System.err.print(pString_);
        System.err.flush();
    }

    /**
     * The same as println, except output goes to std err.
     */
    public static void printlnErr() {
        printErr('\n');
    }

    /**
     * The same as println, except output goes to std err.
     * Unlike using System.err directly this
     * method makes sure the content gets flushed out immediately.
     *
     * @param sMessage_ a string without a trailing newline to send
     *                  to standard error.
     */
    public static void printlnErr(String sMessage_) {
        printErr(sMessage_);
        printlnErr();
    }

    /**
     * Prints out the object to System.err.
     * Unlike using System.err directly this
     * method makes sure the content gets flushed out immediately.
     *
     * @param pObject_ an object that will be converted to a string which
     *                 will be sent to standard error with a newline appended.
     */
    public static void printlnErr(Object pObject_) {
        if (pObject_ == null) {
            printlnErr("null");
        } else {
            printlnErr(pObject_.toString());
        }
    }

    /**
     * Prints out a String with a prefix of the oClass_ class
     * name to System.err.
     */
    public static void printlnErr(Object oClass_, Object oMessage_) {
        assert oClass_ != null;
        printErr(oClass_.getClass().getName() + ".");
        printlnErr(oMessage_);
    }

    /**
     * Prints out a String with a prefix of the oClass_ class
     * name to System.err.
     */
    public static void printlnErr(Class<?> oClass_, Object oMessage_) {
        assert oClass_ != null;
        printErr(oClass_.getName() + ".");
        printlnErr(oMessage_);
    }

    // -----------------------------------------------------
    // basic converter methods
    // -----------------------------------------------------

    /**
     * Converts a byte to an int.
     *
     * @deprecated Use byteToInt(byte) instead.
     */
    public static int btoi(byte b_) {
        return byteToInt(b_);
    }

    /**
     * Converts a byte to an int.
     */
    public static int byteToInt(byte b_) {

        return ((256 + b_) & 255);
    }

    /**
     * Converts a String to a boolean.
     * It's true if the string consists of the word "true"
     * ignoring case, otherwise it returns false.
     *
     * @return (( pString_ ! = null) && pString_.toLowerCase().equals("true"));
     */
    public static boolean atob(String pString_) {
        return (Boolean.parseBoolean(pString_));
    }

    // -----------------------------------------------------
    // string methods
    // -----------------------------------------------------

    /**
     * Tests, if a given String equals null or "".
     */
    public static boolean isEmpty(String sTest_) {
        return sTest_ == null || sTest_.equals("");
    }

    /**
     * Tests if this string ends with the specified character.
     */
    public static boolean endsWith(String sThis_, char cOther_) {
        if (isEmpty(sThis_)) {
            return false;
        }

        return (sThis_.charAt(sThis_.length() - 1) == cOther_);
    }

    /**
     * Tests if this string ends with the second string.
     */
    public static boolean endsWith(String pString_, String sEnd_) {
        if (isEmpty(sEnd_)) {
            return true;
        } else if (isEmpty(pString_)) {
            return false;
        }
        int stringLen = pString_.length();
        int endLen = sEnd_.length();
        if (endLen > stringLen) {
            return false;
        }

        return sEnd_.equals(pString_.substring(stringLen - endLen,
                stringLen));
    }

    /**
     * Replaces all occurences of sOld_ in pString with sNew_.
     * Unlike the String.replace(char, char) method this one accepts whole strings
     * for replacement and as a consequence also allows to delete sub strings.
     *
     * @param pString_ a string that shall get some sub strings replaced.
     * @param sOld_    a string for which all occurences in the first string shall be replaced.
     * @param sNew_    a string which will be used for replacement of the old sub strings.
     * @return the first string provided but with the replaced sub strings.
     */
    public static String replace(String pString_,
                                 String sOld_, String sNew_) {
        return replace(pString_, sOld_, sNew_, 0);
    }

    /**
     * Replaces all occurences of sOld_ in pString with sNew_.
     *
     * @param startIndex_ The startindex_ gives the position in
     *                    pString_ where the replace procedure
     *                    should start.
     */
    public static String replace(String pString_,
                                 String sOld_, String sNew_,
                                 int startIndex_) {
        assert sNew_ != null && sOld_ != null;
        // 23. 11. 1996
        // solange bis old nicht mehr gefunden wird.

        if (pString_ == null) {
            return null;
        }

        StringBuilder sbRetVal = new StringBuilder
                (pString_.length());
        int copyIndex = 0;

        int lengthOld = sOld_.length();
        int index = startIndex_;
        while (true) {
            //Util.debug( "ccl.util.Util.replace(..).index: " + index );

            index = pString_.indexOf(sOld_, index);
            if (index == -1) {
                break;
            }
            sbRetVal.append(pString_, copyIndex, index);
            sbRetVal.append(sNew_);
            index += lengthOld;
            copyIndex = index;
        }

        sbRetVal.append(pString_.substring(copyIndex));

        return sbRetVal.toString();
    }

    /**
     * Before returning pObject_.toString() it checks if
     * pObject_ is null. If so, "null" is returned.
     */
    public static String toString(Object pObject_) {
        if (pObject_ == null) {
            return "null";
        }

        return pObject_.toString();
    }

    /**
     * @return -1 if sToLookIn_ does not contain sThis_.
     * Otherwise the position is returned.
     * @deprecated Well, String.indexOf(String) should be
     * just fine!?
     */
    public static int contains(String sToLookIn_, String sThis_) {
        if (sToLookIn_ == null) {
            return -1;
        }
        if (sThis_ == null) {
            return -1;
        }
        for (int index = 0; index < sToLookIn_.length(); index++) {
            if (sToLookIn_.regionMatches(index, sThis_, 0, sThis_.length())) {
                return index;
            }
        }

        return -1;
    }

    /**
     * If you believe it or not, in a very old jdk version
     * there was a bug in String.compareTo(String) and I did
     * need this as a workaround. This method should be of no
     * use anymore [1999-07-15].
     *
     * @deprecated Use String.compare instead.
     */
    public static int compare(String firstString,
                              String anotherString) {
        int len1 = firstString.length();
        int len2 = anotherString.length();
        int n = Math.min(len1, len2);
        int i = 0;
        int j = 0;

        while (n-- != 0) {
            char c1 = firstString.charAt(i++);
            char c2 = anotherString.charAt(j++);
            if (c1 != c2) {
                return (c1 - c2);
            }
        }

        return (len1 - len2);
    }

    /**
     * @return -1 means the string is either "" or contains just the
     * char cNot_.
     */
    public static int indexOfNot(String pString_, char cNot_,
                                 int startIndex_) {
        assert pString_ != null && startIndex_ > 0;

        while (startIndex_ < pString_.length()) {
            if (pString_.charAt(startIndex_) != cNot_) {
                return startIndex_;
            }
            startIndex_++;
        }

        return -1;
    }

    /**
     * @return -1 means the string is either "" or contains just the
     * char cNot_.
     */
    public static int indexOfNot(String pString_,
                                 char cNot_) {
        return indexOfNot(pString_, cNot_, 0);
    }

    /**
     * How many chars c_ contains the String pString_.
     */
    public static int count(String pString_, char c_) {
        int retVal = 0;

        if (Util.isEmpty(pString_)) {
            return retVal;
        }

        int index = pString_.indexOf(c_);
        while (index != -1) {
            retVal++;
            index = pString_.indexOf(c_, ++index);
        }

        return retVal;
    }

    /**
     * This function takes a String and separates it into different
     * lines. The last line does not need to have a separator character.
     *
     * @param lines_   The number of lines that should be extracted.
     *                 Zero if maximum number of lines is requested.
     * @param cCutter_ The character that separates pString_ into
     *                 different lines
     * @return The single lines do not contain the cCutter_
     * character at the end.
     */
    public static Vector<String> stringToLines(int lines_,
                                               String pString_, char cCutter_) {
        int maxLines = Integer.MAX_VALUE;
        if (lines_ > 0) {
            maxLines = lines_;
        }

        Vector<String> vRetVal = new Vector<>();
        if (pString_ == null) {
            return vRetVal;
        }

        int startIndex = 0;
        for (; maxLines > 0; maxLines--) {
            int endIndex = pString_.indexOf(cCutter_, startIndex);
            if (endIndex == -1) {
                if (startIndex < pString_.length()) {
                    endIndex = pString_.length();
                } else {
                    break;
                }
            }
            String sLine = pString_.substring(startIndex, endIndex);
            vRetVal.addElement(sLine);
            startIndex = endIndex + 1;
        }

        return vRetVal;
    }

    /**
     * This function takes a String and separates it into different
     * lines. The last line does not need to have a separator character.
     *
     * @param cCutter_ The character that separates pString_ into
     *                 different lines
     * @return The single lines do not contain the cCutter_ character
     * at the end.
     */
    public static Vector<String> stringToLines(String pString_, char cCutter_) {
        return stringToLines(0, pString_, cCutter_);
    }

    /**
     * This function takes a String and separates it into different
     * lines. The last line does not need to have a '\n'. The function
     * can't handle dos carriage returns.
     *
     * @return The single lines do not contain the '\n' character
     * at the end.
     */
    public static Vector<String> stringToLines(String pString_) {
        return stringToLines(pString_, '\n');
    }

    // -----------------------------------------------------
    // system methods
    // -----------------------------------------------------

    /**
     * This method does return immediately. If you want the
     * output of the process use either systemAndWait() or
     * systemAndGetError().
     *
     * @throws IOException Whatever can go wrong.
     */
    public static Process system(String sCommand_)
            throws IOException {
        return Runtime.getRuntime().exec(sCommand_);
    }

    /**
     * Execute an external command.
     *
     * @throws IOException Whatever Runtime.exec(..) throws.
     */
    public static Process system(String[] asCommand_)
            throws IOException {

        return Runtime.getRuntime().exec(asCommand_);
    }

    // -----------------------------------------------------
    // date methods
    // -----------------------------------------------------

    /**
     * Returns string representation of a date in
     * standard ISO format.
     *
     * @return 1998-12-06 for example.
     * @deprecated use 'getDate( Date )' instead.
     */
    public static String getStandardDate(Date pDate_) {
        return getDate(pDate_);
    }

    /**
     * Returns string representation of a date in
     * standard ISO format.
     *
     * @return 1998-12-06 for example.
     */
    public static String getDate(Date pDate_) {
        SimpleDateFormat pSimpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd");

        return pSimpleDateFormat.format(pDate_);
    }

    /**
     * Returns the current date as an ISO date string.
     *
     * @return 1998-12-06 for example.
     */
    public static String getDate() {
        return getDate(getCalendar());
    }

    /**
     * Returns the given date as an ISO date string.
     *
     * @return 1998-12-06 for example.
     */
    public static String getDate(Calendar pCalendar_) {
        SimpleDateFormat pSimpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd");

        pSimpleDateFormat.setCalendar(pCalendar_);

        return pSimpleDateFormat.format(pCalendar_.getTime());
    }

    /**
     * This is a replacement of the SimpleTimeZone.getTimeZone(String)
     * function that additionally creates a GregorianCalendar of the
     * given timezone. There is a new timezone 'CET' (Central
     * European Time. It has the official daylight saving time settings
     * (ranging from the last Sunday in March at 2:00 am to the last
     * Sunday in October at 2:00 am) and should be preferred over 'ECT'.
     *
     * @param sTimeZoneID_ If it is null or "" then "GMT" is used.
     */
    public static Calendar getCalendar(String sTimeZoneID_) {
        if (Util.isEmpty(sTimeZoneID_)) {
            sTimeZoneID_ = "GMT";
        }
        TimeZone pTimeZone;
        if (sTimeZoneID_.equals("UTC")) {
            pTimeZone = new SimpleTimeZone(0, "UTC");
        } else {
            pTimeZone = SimpleTimeZone.getTimeZone(sTimeZoneID_);
        }
        Util.debug("Util.getCalendar(): pTimeZone: " + pTimeZone);
        // New Daylight Saving Time Convention in 35 European Countries
        if (sTimeZoneID_.equals("CET")) {
            pTimeZone = new SimpleTimeZone(1000 * 60 * 60
                    , "CET"
                    , Calendar.MARCH, -1
                    , Calendar.SUNDAY
                    , 2 * 1000 * 60 * 60
                    , Calendar.OCTOBER
                    , -1
                    , Calendar.SUNDAY
                    , 2 * 1000 * 60 * 60);
        }
        Util.debug("Util.getCalendar(): pTimeZone: " + pTimeZone);

        return new GregorianCalendar(pTimeZone);
    }

    /**
     * @return Calendar with local timezone
     */
    public static Calendar getCalendar() {
        return getCalendar(Calendar.getInstance().getTimeZone().getID());
    }
}
