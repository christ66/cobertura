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

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Utility class for file operations.<p>
 * <p>
 * Simple but most commonly used methods of this class are:<br>
 * - {@link #readFile(java.lang.String) readFile}<br>
 * - {@link #concatPath(java.lang.String, java.lang.String) concatPath}<br>
 * - {@link #exists(java.lang.String) exists}<br>
 * - {@link #existsDir(java.lang.String) existsDir}<br>
 * - {@link #existsFile(java.lang.String) existsFile}<p>
 * <p>
 * Other less frequently used but still handy methods are:<br>
 * - {@link #normalizeFileName(java.lang.String) normalizeFileName} to take the current user directory into account via the 'user.dir' system property<br>
 * <p>
 * There are also the standard file operation methods available.
 * Some of these are named for convenience and easy memorization after their Unix
 * counterparts, like {@link #mv(java.lang.String, java.lang.String) mv}, {@link #mkdir(java.lang.String) mkdir} ({@link #md(java.lang.String) md})}.
 *
 * @author <a href="http://www.kclee.com/clemens/">
 * Chr. Clemens Lee</a>
 * &lt;<a href="mailto:clemens@kclee.com">
 * clemens@kclee.com
 * </a>>
 * @version $Id: FileUtil.java,v 1.61 2003/10/18 07:53:20 clemens Exp clemens $
 */
public class FileUtil {
    /**
     * Utility class which should never instanciate itself.
     */
    private FileUtil() {
        super();
    }

    /**
     * You give it a package name and it looks with the
     * help of the classpath on the file system if it can
     * find a directory that relates to this package.
     *
     * @return Includes the local path of the package too.
     * If no path could be found, "" is returned.
     */
    public static String getPackagePath(String sPackageName_) {
        return getPackagePath(sPackageName_,
                System.getProperty("java.class.path"));
    }

    /**
     * You give it a package name and it looks with the
     * help of the classpath on the file system if it can
     * find a directory that relates to this package.<p>
     * <p>
     * Todo: what happens with an empty classpath? That should be
     * equivalent to a "." classpath.
     *
     * @return includes the local path of the package too.
     * If no path could be found, "" is returned.
     */
    public static String getPackagePath(String sPackageName_,
                                        String sClassPath_) {
        Util.debug("ccl.util.FileUtil.getPackagePath(..).sPackageName_: " +
                sPackageName_);

        assert !Util.isEmpty(sPackageName_);
        assert !Util.isEmpty(sClassPath_);

        // remove leading dots
        int index = Util.indexOfNot(sPackageName_, '.');
        String sPackagePath = "";
        if (index > -1) {
            sPackagePath = sPackageName_.
                    substring(index);
        }

        // '.' -> '/'
        sPackagePath = sPackagePath.replace
                ('.', File.separatorChar);

        Vector vClassPaths = Util.stringToLines
                (sClassPath_, File.pathSeparatorChar);
        if (vClassPaths.size() == 0) {
            vClassPaths.addElement(".");
        }
        Enumeration eClassPaths = vClassPaths.elements();
        while (eClassPaths.hasMoreElements()) {
            String sNextPath = (String) eClassPaths.nextElement();
            sNextPath = concatPath(sNextPath, sPackagePath);
            Util.debug("ccl.util.FileUtil.getPackagePath(..).sNextPath: " +
                    sNextPath);
            if (existsDir(sNextPath)) {
                return (sNextPath);
            }
        }

        return "";
    }

    /**
     * Does work only when class exists outside a zip or jar file.
     *
     * @return Includes the local path of the package too.
     */
    public static String getClassPath(Object oClass_) {
        Util.debug("ccl.util.FileUtil.getClassPath(..).oClass_: "
                + String.valueOf(oClass_));

        if (oClass_ == null) {
            return null;
        }

        String sClassName = oClass_.getClass().getName();
        int index = sClassName.lastIndexOf('.');
        String sPackageName = ".";
        if (index != -1) {
            sPackageName = sClassName.substring
                    (0, sClassName.lastIndexOf('.'));
        }
        Util.debug("ccl.util.FileUtil.getClassPath(..).sPackageName: "
                + sPackageName);
        String sPackagePath = getPackagePath(sPackageName);

        return sPackagePath;
    }

    /**
     * Concatenates a file path with the file name. If
     * necessary it adds a File.separator between the path
     * and file name. For example "/home" or "/home/" and "clemens" both
     * become "/home/clemens".<p>
     * <p>
     * This method is inspired from the FrIJDE project out
     * of the gCollins.File.FileTools class.<p>
     * <p>
     * FrIJDE Homepage:
     * http://amber.wpi.edu/~thethe/Document/Besiex/Java/FrIJDE/
     *
     * @param sPath_ a directory path. Is not allowed to be null.
     * @param sFile_ the base name of a file.
     * @return sPath_ if sFile_ is empty.
     */
    public static String concatPath(String sPath_, String sFile_) {
        assert sPath_ != null;
        Util.debug("ccl.util.FileUtil.concatPath(..).sPath_: --->" +
                sPath_ + "<---");
        Util.debug("ccl.util.FileUtil.concatPath(..).sFile_: " +
                sFile_);

        String sRetVal = sPath_;

        if (!Util.isEmpty(sFile_)) {
            if (sPath_.length() > 0
                    && (!Util.endsWith(sPath_, File.separatorChar))) {
                sRetVal += File.separator;
            }

            sRetVal += sFile_;
        }

        return sRetVal;
    }

    /**
     * Reads a stream, gives back a string.
     *
     * @throws FileNotFoundException if file does not exist.
     * @throws IOException           if any file operation fails.
     */
    public static String readStream(InputStream stream_)
            throws IOException,
            FileNotFoundException {
        StringBuffer sFileContent = new StringBuffer(100000);

        InputStreamReader streamReader = new InputStreamReader(stream_);
        if (streamReader != null) {
            BufferedReader brIni = new BufferedReader(streamReader);
            if (brIni != null) {
                while (brIni.ready()) {
                    String sLine = brIni.readLine();
                    if (sLine == null) {
                        break;
                    }
                    sFileContent.append(sLine).append('\n');
                }
                brIni.close();
            }
            streamReader.close();
        }

        return sFileContent.toString();
    }

    /**
     * Reads a File and returns the content in a String.
     * CRLF -> LF conversion takes place. This is a convenience method so you don't
     * need to bother creating a file reader object and closing it after it has
     * been used.
     *
     * @param sFileName_ the name of the file to read.
     * @return a string with the content of the file but without
     * any CR characters.
     * @throws FileNotFoundException if file does not exist.
     * @throws IOException           if any file operation fails.
     */
    public static String readFile(String sFileName_)
            throws IOException
            , FileNotFoundException {
        StringBuffer sFileContent = new StringBuffer(100000);

        try {
            FileReader frIni = new FileReader(sFileName_);
            if (frIni != null) {
                BufferedReader brIni = new BufferedReader(frIni);
                if (brIni != null) {
                    while (brIni.ready()) {
                        String sLine = brIni.readLine();
                        if (sLine == null) {
                            break;
                        }
                        sFileContent.append(sLine).append('\n');
                    }
                    brIni.close();
                }
                frIni.close();
            }
        } catch (FileNotFoundException fileNotFoundException) {
            throw new FileNotFoundException("No such file: '" + sFileName_ + "'");
        }

        return sFileContent.toString();
    }

    /**
     * True if a specified file exists.
     */
    public static boolean existsFile(String sFileName_) {
        assert sFileName_ != null;

        File pFile = new File(sFileName_);

        return (pFile.isFile());
    }

    /**
     * Tests, if a given directory exists.
     */
    public static boolean existsDir(String sDirName_) {
        assert sDirName_ != null;

        File pFile = new File(sDirName_);
        return (pFile.isDirectory());
    }

    /**
     * True if a specified object on the file system is either
     * a file or a directory.
     */
    public static boolean exists(String sFileOrDirName_) {
        assert sFileOrDirName_ != null;
        return (existsFile(sFileOrDirName_) ||
                existsDir(sFileOrDirName_));
    }

    /**
     * Returns a Vector with all file names that are inside the
     * specified directory. <br>
     * For example: FileUtil.getFiles("C:\", ".txt")
     *
     * @return Not the full path names are returned, just the simple
     * file names.
     */
    public static Vector getFilteredDirContent(String sDir_,
                                               FilenameFilter pFilenameFilter_) {
        Util.debug("ccl.util.FileUtil.getFilteredDirContent(..).sDir_: " +
                sDir_);
        assert sDir_ != null;
        File pFile = new File(sDir_);
        assert pFile.isDirectory();

        String asDirContent[] = pFile.list(pFilenameFilter_);
        Util.debug("ccl.util.FileUtil.getFilteredDirContent(..)"
                + ".asDirContent.length: "
                + asDirContent.length);

        Vector vRetVal = new Vector();
        for (int index = 0; index < asDirContent.length; index++) {
            vRetVal.addElement(asDirContent[index]);
        }
        Util.debug("ccl.util.FileUtil.getFilteredDirContent(..).vRetVal: " +
                vRetVal);

        return (vRetVal);
    }

    /**
     * @see #getFilteredDirContent(java.lang.String, java.io.FilenameFilter)
     * @deprecated
     */
    public static Vector _getFilteredDirContent(String sDir_,
                                                FilenameFilter pFilenameFilter_) {
        return getFilteredDirContent(sDir_, pFilenameFilter_);
    }

    /**
     * Remove file on file system.
     *
     * @return true if error.
     */
    public static boolean delete(String sFileName_) {
        assert sFileName_ != null;
        boolean bRetVal = false;
        boolean bExists = exists(sFileName_);
        if (!bExists) {
            // job done
            return bRetVal;
        }
        try {
            File flTemp = new File(sFileName_);
            bRetVal = !(flTemp.delete());
        } catch (SecurityException pSecurityException) {
            return true;
        }

        return bRetVal;
    }

    /**
     * Renames or moves a file. Be aware that the old file at the
     * destination will be deleted without a warning.
     *
     * @return true if an error occurred. false if sSource_ is not
     * existent.
     * @see #mv
     */
    public static boolean move(String sSource_, String sDest_) {
        assert sSource_ != null && sDest_ != null;
        try {
            File flSource = new File(sSource_);
            File flDest = new File(sDest_);
            delete(sDest_);

            return !flSource.renameTo(flDest);
        } catch (Exception pException) {
        }

        return true;
    }

    /**
     * Renames or moves a file. Be aware that the old file at the
     * destination will be deleted without a warning. This is a
     * shortcut for method 'move'.
     *
     * @return true if an error occurred. false if sSource_ is not
     * existent.
     * @see #move
     */
    public static boolean mv(String sSource_, String sDest_) {
        return move(sSource_, sDest_);
    }

    /**
     * Creates the specified directory and if necessary any
     * parent directories. It is a shortcut for 'mkdir'.
     *
     * @return true if an error occured. Note that this is
     * vice versa to the File.mkdirs() behavior.
     * @see File#mkdirs()
     */
    public static boolean md(String sFullDirName) {
        boolean bError = false;

        try {
            File flDir = new File(sFullDirName);
            bError = !flDir.mkdirs();
        } catch (Exception e) {
            bError = true;
        }

        return bError;
    }

    /**
     * Creates the specified directory and if necessary any
     * parent directories.
     *
     * @return true if an error occured. Note that this is
     * vice versa to the File.mkdirs() behavior.
     * @see File#mkdirs()
     */
    public static boolean mkdir(String sFullDirName) {
        return md(sFullDirName);
    }

    /**
     * @return It's the canonical path of sFileName_.
     */
    public static String getAbsoluteFileName(String sFileName_) {
        String sRetVal = null;

        try {
            File pFile = new File(sFileName_);
            sRetVal = pFile.getCanonicalPath();
        } catch (Exception e) {
            return null;
        }

        return sRetVal;
    }

    /**
     * The same as getAbsoluteFileName(..).
     *
     * @return It's the canonical path of sFileName_.
     */
    public static String getAbsolutePath(String sFileName_) {
        return getAbsoluteFileName(sFileName_);
    }

    /**
     * This method returns an absolute (canonical)
     * file name. The difference to getAbsoluteFileName
     * is that this method uses the system property
     * "user.dir" instead of the native system's current
     * directory. This way you get a chance of changing
     * the current directory inside Java and let your
     * program reflect that change.
     */
    public static String normalizeFileName(String sFile) {
        return normalizeFileName(sFile
                , (String) System.getProperties()
                        .get("user.dir")
        );
    }

    /**
     * This method returns an absolute (canonical)
     * file name. The difference to getAbsoluteFileName
     * is that this method uses the system property
     * sUserDir instead of the native system's current
     * directory. This way you get a chance of changing
     * the current directory inside Java and let your
     * program reflect that change.
     */
    public static String normalizeFileName(String sFile, String sUserDir) {
        sFile = sFile.trim();
        if (Util.isEmpty(sFile)
                || sFile.equals(".")) {
            sFile = sUserDir;
        } else if (!FileUtil.isAbsolute(sFile)) {
            sFile = FileUtil.concatPath(sUserDir
                    , sFile);
        }
        sFile = FileUtil.getAbsoluteFileName(sFile);

        return sFile;
    }

    /**
     * Returns a temporary directory. This method will be upwards compatible
     * to jdk 1.2. It uses the java property "java.io.tempdir". If this is
     * not set like in jdk 1.1, "user.home" + "/tmp" will be used. If it does
     * not yet exist we take the freedom to create it. If a $HOME/tmp _file_
     * exists already, it will be deleted!!!
     */
    public static String getTempDir() {
        // for jdk 1.2 use environment var
        String tempDir = System.getProperty("java.io.tmpdir");

        // for jdk 1.1 use $HOME/tmp
        // create it if it does not exist (inclusive remove it if it's a file
        if (tempDir == null) {
            tempDir = System.getProperty("user.home");
            tempDir = concatPath(tempDir, "tmp");
            if (existsFile(tempDir)) {
                FileUtil.delete(tempDir);
            }
            if (!FileUtil.existsDir(tempDir)) {
                FileUtil.md(tempDir);
            }
        }

        return tempDir;
    }

    /**
     * Tests if the file represented by this File object is an absolute
     * pathname. The definition of an absolute pathname is system
     * dependent. For example, on UNIX, a pathname is absolute if its first
     * character is the separator character. On Windows
     * platforms, a pathname is absolute if its first character is an
     * ASCII '\' or '/', or if it begins with a letter followed by a colon.
     */
    public static boolean isAbsolute(String sFileName_) {
        assert Util.isEmpty(sFileName_);

        return new File(sFileName_).isAbsolute();
    }

    /**
     * There is one big advantage this method has over
     * Class.getResourceAsStream(..). There are three
     * different circumstances from where you want to load
     * a resource, only two work by the default JDK
     * ClassLoader resource location method.
     * First case, your resource file is in the same directory
     * as your class file just on a normal file system.
     * Second case, your resource file is inside a jar file.
     * This both is handled by the normal ClassLoader.
     * But what if you have a src and a classes directory.
     * Then you want your resource file in the src directory
     * tree without the need to copy the resource file over
     * to the classes directory tree. If you stick to the
     * 'classes' and 'src' directory name convention, this
     * method still finds the resource in the src directory.
     *
     * @see java.lang.Class#getResourceAsStream(java.lang.String)
     */
    public static InputStream getResourceAsStream(Object pObject_,
                                                  String sRecourceName_) {
        InputStream isResource = pObject_.getClass().
                getResourceAsStream(sRecourceName_);
        if (isResource == null) {
            String sPath = getClassPath(pObject_);
            Util.debug("ccl.util.FileUtil.getResourceAsStream(..).sPath: " +
                    sPath);

            if (sPath.equals("classes")) {
                sPath = "src";
            } else if (Util.endsWith(sPath, File.separator + "classes")) {
                sPath = sPath.substring(0, sPath.length() - 7) + "src";
            } else if (sPath.startsWith("classes" + File.separator)) {
                sPath = "src" + sPath.substring(7);
            } else {
                int index = sPath.lastIndexOf
                        (File.separator +
                                "classes" + File.separator);
                if (index != -1) {
                    sPath = sPath.substring(0, index + 1) +
                            "src" + sPath.substring(index + 8);
                }
            }
            try {
                isResource = new FileInputStream
                        (concatPath(sPath,
                                sRecourceName_));
            } catch (Exception pException) {
                isResource = null;
            }
        }

        return isResource;
    }

    /**
     * Get the base name of a file. This is the name only,
     * without the path information.
     *
     * @param sFileName_ a string with a file name.
     * @return the name of the file itself,
     * e.g. "foo/README.TXT" -> "README.TXT".
     * @deprecated use getBaseName.
     */
    public static String getBaseFileName(String sFileName_) {
        return new File(sFileName_).getName();
    }

    /**
     * Get the base name of a file. This is the name only,
     * without the path information.
     *
     * @param sFileName_ a string with a file name.
     * @return the name of the file itself,
     * e.g. "foo/README.TXT" -> "README.TXT".
     */
}
