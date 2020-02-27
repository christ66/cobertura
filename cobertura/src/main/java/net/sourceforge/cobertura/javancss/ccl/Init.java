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

import java.awt.Color;
import java.awt.SystemColor;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * Manages the ini file and configuration stuff for an
 * application.<p>
 *
 * Right now localisation of ini file does not work with
 * src and classes directory separation.
 * When the ini file is inside an archive, user can't
 * change the ini file values.
 * In the future there should be one application ini file
 * that can be located like a resource and a user ini file
 * located in the user's home directory.<p>
 *
 * @version $Id: Init.java,v 1.57 2003/05/01 16:44:40 clemens Exp clemens $
 * @author <a href="http://www.kclee.com/clemens/">
 *         Chr. Clemens Lee</a>
 *         &lt;<a href="mailto:clemens@kclee.com">
 *         clemens@kclee.com
 *         </a>>
 */
public class Init 
{
    private static final String S_PROJECTSUFFIX_KEY = "ProjectSuffix";
    private static final String S_INI_SUFFIX        = ".ini";
    private static final String S_INIFILE_INIT          = "Init";
    private static final String S_INIFILE_INIT_FILE     = "File";
    private static final String S_INIFILE_INIT_OLDFILES = "OldFiles";
    private static final String S_INIFILE_INIT_DEBUG    = "Debug";
    private static final String S_INIFILE_INIT_AUTHOR   = "Author";
    private static final String S_INIFILE_HELP       = "Help";
    private static final String S_INIFILE_HELP_USAGE = "HelpUsage";
    private static final String S_INIFILE_COLORS          = "Colors";
    private static final String S_INIFILE_SYSTEM_COLORS   = "UseSystemColors";
    private static final String S_INIFILE_COLORS_BACK     = "Background";
    private static final String S_INIFILE_COLORS_LISTBACK = "ListBackground";
    private static final String S_INIFILE_COLORS_LISTFORE = "ListForeground";
    private static final String S_INIFILE_COLORS_LISTHIGHLIGHT 
        = "ListHighlight";
    private static final String S_OPTION_STRING = " string";
   
    private int    _maxProjects = 9;
    private String _sInfoHeader;
   
    /*private Color _clrBackground     = Color.gray;
    private Color  _clrForeground     = Color.black;
    private Color  _clrListBackground = Color.gray;
    private Color  _clrListForeground = Color.black;
    private Color  _clrListHighlight  = Color.black;*/
    private Color  _clrBackground     = null;
    private Color  _clrForeground     = null;
    private Color  _clrListBackground = null;
    private Color  _clrListForeground = null;
    private Color  _clrListHighlight  = null;
    private Color  _clrTextBackground = null;
    private Color  _clrTextForeground = null;

    private String _sBackground       = null;
    private String _sForeground       = null;
    private String _sListBackground   = null;
    private String _sListForeground   = null;
    private String _sListHighlight    = null;
    private String _sTextBackground   = null;
    private String _sTextForeground   = null;

    /**
     * This field gets set when ini file has an Author field
     * or with method setAuthor(..).
     */
    private String _sAuthor = "<unknown author>";
    private String _sFileFullName = "";
    private String _sApplicationName = null;
    private String _sApplicationPath = null;
    private String _sFullIniFileName = null;
    
    private Hashtable _hshKeyValues = new Hashtable();
    
    private Hashtable _htOptions    = new Hashtable();
    private Vector    _vArguments   = new Vector();
    
    /**
     * Reihenfolge der alten Files: �ltestes File an Position 0
     */
    private Vector    _vOldFiles    = new Vector();

    private boolean _bSurpressInifileOption = false;
    
    private String _sIniFileContent = null;

    /**
     * If no archive with the same name as the application
     * was found, "." is returned.
     * If succesful a string like ".../appname3.7/lib/" is returned.
     */
    static private String _getArchivePath(String sApplicationName_) 
    {
        String sClasspath = System.getProperty("java.class.path");

        return _getArchivePath( sApplicationName_, sClasspath );
    }

    static private String _getArchivePath( String sApplicationName_
                                           , String sClassPath      )
    {
        Vector vClasspath = Util.stringToLines
               (sClassPath, File.pathSeparatorChar);
        for(Enumeration eClasspath = vClasspath.elements();
            eClasspath.hasMoreElements(); )
        {
            String sNextPath = (String) eClasspath.nextElement();
            if (Util.endsWith(sNextPath, ".zip") ||
                Util.endsWith(sNextPath, ".jar"))
            {
                int lastSeparator = sNextPath.lastIndexOf
                       (File.separatorChar);
                String sArchiveName = sNextPath.substring
                       (lastSeparator + 1,
                        sNextPath.length() - 4);
                Util.debug( "ccl.util.Init._getArchivePath(..).sArchiveName: "
                            + sArchiveName );
                if (sArchiveName.startsWith(sApplicationName_)) 
                {
                    String sApplicationPath = sNextPath.
                           substring(0, lastSeparator + 1);
                    if (Util.isEmpty(sApplicationPath)) 
                    {
                        sApplicationPath = ".";
                    }
                    return sApplicationPath;
                }
            }
        }
        
        return ".";
    }
    
    /**
     * in Classpath relative Pfade durch absolute ersetzen
     *
     * @deprecated   Use FileUtil.getAbsolutePathList(String) instead.
     */
    private void _setClassPath() 
    {
        String sStartPath = System.getProperty("user.dir");
        Util.debug("_setClassPath: sStartPath: " + sStartPath);
        String sClassPath = System.getProperty("java.class.path");
        Util.debug("_setClassPath: sClassPath: " + sClassPath);
        Vector vClassPaths = Util.stringToLines(sClassPath,
                                                File.pathSeparatorChar);
        Util.debug("_setClassPath: vClassPaths: " + vClassPaths);
        for(int classPath = 0; classPath < vClassPaths.size();
            classPath++)
        {
            String sSingleClassPath = (String) vClassPaths.elementAt(classPath);
            Util.debug("_setClassPath: sSingleClassPath: " + sSingleClassPath);
            if (sSingleClassPath.startsWith(".")) 
            {
                sSingleClassPath = FileUtil.concatPath(sStartPath,
                                                       sSingleClassPath);
                try 
                {
                    File flSingleClassPath = new File(sSingleClassPath);
                    sSingleClassPath = flSingleClassPath.getCanonicalPath();
                }
                catch(Exception e) 
                {
                }
                vClassPaths.setElementAt(sSingleClassPath, classPath);
            }
        }
        sClassPath = Util.concat(vClassPaths,
                                 File.pathSeparatorChar);
        if ((sClassPath.length() > 0) &&
            !Util.endsWith(sClassPath, File.pathSeparatorChar))
        {
            sClassPath += Util.cToS(File.pathSeparatorChar);
        }
        Util.debug("_setClassPath: sClassPath: " + sClassPath);
        Properties pProperties = System.getProperties();
        pProperties.put("java.class.path", sClassPath);
    }

    /** 
     * This flag informs this object if the colors have been already 
     * initialized. 
     */
    private boolean _bColorsInitialized = false;

    /**
     * Color handling: batch programs have no need for colors
     * and colors do get the awt into play. Therefore we predefine
     * colors and store their values in strings. Only when the
     * user requests a color for the first time do we convert them
     * to a color object.
     */
    private void _initColors() 
    {
        Vector vRGBString = null;
        
        // define defaults
        _sBackground     = "192 192 192";
        _sForeground     = "0 0 0";
        _sListBackground = _sBackground;
        _sListForeground = _sForeground;
        _sListHighlight  = _sListForeground;
        _sTextBackground = "255 255 255";
        _sTextForeground = _sForeground;
        
        // user system colors?
        boolean bSystemColors = Util.atob
               ( getKeyValue( S_INIFILE_COLORS,
                              S_INIFILE_SYSTEM_COLORS ) );
        Util.debug( this, "_init().bSystemColors: " + bSystemColors );
        if ( bSystemColors ) 
        {
            _clrBackground = new Color( SystemColor.window.getRGB() );
            Util.debug( this, "_init().SystemColor: " + _clrBackground );
        }
        else 
        {
            String sColor = getKeyValue( S_INIFILE_COLORS
                                         , S_INIFILE_COLORS_BACK );
            if ( !Util.isEmpty( sColor ) ) 
            {
                _sBackground = sColor;
            }
        }
        Util.debug( this, "_initColors()._sBackground: " + _sBackground );
    
        // ListHintergrund Farbe
        String sColor = getKeyValue(S_INIFILE_COLORS,
                                    S_INIFILE_COLORS_LISTBACK);
        if ( !Util.isEmpty( sColor ) ) 
        {
            _sListBackground = sColor;
        }

        // ListFordergrund Farbe
        sColor = getKeyValue(S_INIFILE_COLORS, S_INIFILE_COLORS_LISTFORE);
        if ( !Util.isEmpty( sColor ) ) 
        {
            _sListForeground = sColor;
        }


        // List Highlight Color
        sColor = getKeyValue(S_INIFILE_COLORS, S_INIFILE_COLORS_LISTHIGHLIGHT);
        if ( !Util.isEmpty( sColor ) ) 
        {
            _sListHighlight = sColor;
        }

        _bColorsInitialized = true;
    }   

    /** 
     * Get Application Path.
     */
    static public String getApplicationPath( String sApplicationName 
                                             , String sPackageName   
                                             , String sClassPath )
    {
        String sApplicationPath = _getArchivePath
               ( sApplicationName.toLowerCase()
                 , sClassPath );
        Util.debug( "ccl.util.Init.getApplicationPath(..).sApplicationPath.1: " 
                    + sApplicationPath );
        if ( sApplicationPath.equals( "." ) ) 
        {
            // no archive with same name found
            Util.debug( "ccl.util.Init.getApplicationPath(..).sPackageName: " 
                        + sPackageName );
            sApplicationPath = FileUtil.getPackagePath( sPackageName
                                                        , sClassPath );
            Util.debug( "ccl.util.Init.getApplicationPath>(..)"
                        + ".sApplicationPath.2: "
                        + sApplicationPath );
            if ( Util.isEmpty( sApplicationPath ) ) 
            {
                // another search for applicationname.jar
                sApplicationPath = _getArchivePath( sPackageName, sClassPath );
                Util.debug( "ccl.util.Init.<init>(..).sApplicationPath.3: " 
                                  + sApplicationPath );
            }
        }
        // use jacob instead of jacob/lib
        sApplicationPath = FileUtil.getAbsolutePath( sApplicationPath );
        Util.debug( "ccl.util.Init.getApplicationPath(..).sApplicationPath.4: " 
                          + sApplicationPath );
        if ( sApplicationPath.length() > 0 
             && (sApplicationPath.charAt( sApplicationPath.length() - 1 ) 
                 != File.separatorChar) )
        {
            sApplicationPath += File.separator;
        }
        if ( sApplicationPath.endsWith( File.separator 
                                        + "lib" 
                                        + File.separator ) ) 
        {
            sApplicationPath = sApplicationPath.substring
                   ( 0, sApplicationPath.lastIndexOf( File.separator 
                                                      + "lib" 
                                                      + File.separator ) );
        } 
        else if ( sApplicationPath.indexOf( File.separator 
                                            + "classes" 
                                            + File.separator ) != -1 ) 
        {
            // use jacob instead of jacob/classes/jacob
            sApplicationPath = sApplicationPath.substring
                   ( 0, sApplicationPath.lastIndexOf( File.separator 
                                                      + "classes" 
                                                      + File.separator ) );
        }
        // use . instead of lib; can this ever happen after having an absolute 
        // path already?
        if ( sApplicationPath.equals( "lib" ) 
             || sApplicationPath.equals( "lib" + File.separator ) )
        {
            sApplicationPath = ".";
        }
        // _sApplicationPath is now set

        return sApplicationPath;
    }

    /**
     * This was the former Constructor.
     * Init colors and stuff.
     *
     * @see #<init>(Exitable, String[], String)
     */
    private void _init() 
    {
        boolean bDebug = Util.atob(getKeyValue
                                   (S_INIFILE_INIT_DEBUG));
        if (bDebug) 
        {
            Util.setDebug(bDebug);
        }
        
        // Hintergrund Farbe
        /* _initColors(); */

        /*// Language
          String sLanguageName = getKeyValue("Language");
          if (sLanguageName != null) {
          Language lngTemp = (Language)Language.get(sLanguageName);
          if (lngTemp != null) {
          _pLanguage = lngTemp;
          }
          }*/
        
        // FileName
        _sFileFullName = getKeyValue("File");
        Util.debug("Init: <init>: _sFileFullName: " + _sFileFullName);
        if (_sFileFullName == null) 
        {
           _sFileFullName = "";
        }
        
        String sOldFiles = getKeyValue(S_INIFILE_INIT_OLDFILES);
        int i = 0;
        for (int j = 0; j < _maxProjects; j++) 
        {
            int k = sOldFiles.indexOf(59, i);
            if (k == -1)
            {
                break;
            }
            String string3 = sOldFiles.substring(i, k);
            _vOldFiles.addElement(new FileObject(string3));
            i = k + 1;
        }

        // author
        String tempAuthor = getKeyValue( S_INIFILE_INIT_AUTHOR );
        Util.debug( "_init().tempAuthor: " + tempAuthor );
        if ( !Util.isEmpty( tempAuthor ) ) 
        {
            _sAuthor = tempAuthor;
        }
    }

    /**
     * Converts a string (e.g. "192 192 192") into a color.
     *                         
     * @param    sInitColor_   a string with rgb values in
     *                         decimal separated by spaces.
     *                         
     * @return                 an awt Color object.
     */
    static private Color _stringToColor( String sInitColor_ ) 
    {
        int red   = 0;
        int green = 0;
        int blue  = 0;
        try 
        {
            Vector vRGB = Util.stringToLines( sInitColor_, ' ' );
            red   = Util.atoi( (String) vRGB.elementAt( 0 ) );
            green = Util.atoi( (String) vRGB.elementAt( 1 ) );
            blue  = Util.atoi( (String) vRGB.elementAt( 2 ) );
        }
        catch( ArrayIndexOutOfBoundsException arrayException ) 
        {
            Util.printlnErr( "ccl.util.Init._stringToColor(..).sInitColor_: -->"
                               + sInitColor_ 
                               + "<--" );
            arrayException.printStackTrace();
        }
        Color  colRetVal = new Color( red, green, blue );

        return colRetVal;
    }
    
    /**
     * If sIniFileContent_ is null for the ini file will be first looked for in 
     * package/exitable.ini and if not found in package/package.ini.<p>
     *
     * Following some implementation details:<p>
     *
     * If the sIniFileContent_ parameter is set, this will
     * be used when reading ini content.<p>
     *
     * All relative elements in the classpath will be exchanged
     * with their absolute counter parts.<p>
     *
     * Ini file location:<br>
     * 1. Set by user as option.
     *
     * @param   pExitable_         the controlling application object.
     * @param   asArg_             the original command line arguments forwarded
     *                             from the main method.
     * @param   sInfoHeader_       an RCS Header string from the application 
     *                             Main class.
     * @param   sIniFileContent_   default content for an ini file.
     */
    public Init( Exitable   pExitable_
                 , String[] asArg_
                 , String   sInfoHeader_
                 , String   sIniFileContent_ )
    {
        super();

        if (asArg_ == null) 
        {
            asArg_ = new String[0];
        }

        // set debug if given as option
        _setDebug( asArg_ );

        _sIniFileContent = sIniFileContent_;
        Util.debug( this, "<init>(..)._sIniFileContent: " + _sIniFileContent );

        _sInfoHeader = sInfoHeader_;
        
        String sClassName = pExitable_.getClass().getName();
        Util.debug("Init.<init>(..).sClassName: " + sClassName);
         
        _setClassPath();
         
        // Get the Package Name
        String sPackageName = ".";
        if ( sClassName.indexOf('.') == -1 ) 
        {
            _sApplicationName = new String(sClassName);
        }
        else 
        {
            String sTempClassName = sClassName.substring
                    ( sClassName.lastIndexOf( '.' ) + 1 );
            if ( !sTempClassName.equals( "Main" ) &&
                 !sTempClassName.equals( "Controller" ) &&
                 !sTempClassName.equals( "ViewController" ))
            {
                sPackageName = sClassName.substring
                       ( 0, sClassName.lastIndexOf( '.' ) );
                _sApplicationName = sClassName.substring
                       ( sClassName.lastIndexOf( '.' ) + 1 );
            } 
            else 
            {
                sPackageName = sClassName.
                       substring(0, sClassName.lastIndexOf('.'));
                _sApplicationName = sPackageName.substring
                       ( sPackageName.lastIndexOf('.') + 1
                         , sPackageName.length() );
                _sApplicationName 
                       = Util.firstCharToUpperCase(_sApplicationName);
            }
        }
        Util.debug( this, "<init>(..)._sApplicationName: " + _sApplicationName);
        
        // Get Application Path
        _sApplicationPath = getApplicationPath( _sApplicationName
                                                , sPackageName
                                                , System.getProperty
                                                      ( "java.class.path") );

        // First get options names and arguments
        Util.panicIf(asArg_ == null);
        for(int nr = 0; nr < asArg_.length; nr++) 
        {
            Util.debug("Init.<init>(..).asArg_[" + nr + "]: " + asArg_[nr]);
            if (asArg_[nr].charAt(0) == '-') 
            {
                                // is it inifile option?
                if (asArg_[nr].equals("-inifile")) 
                {
                    if (nr + 1 >= asArg_.length) 
                    {
                        Util.println("Error: No ini file name specified.");
                        pExitable_.setExit();
                        
                        return;
                    }
                    _sFullIniFileName = new String(asArg_[nr + 1]);
                    break;
                }
            }
        }
        
        // set full Ini file name
        // this is too complicated
        if (Util.isEmpty(_sFullIniFileName)) 
        {
            String sTempClassName = pExitable_.getClass   ()
                                              .getName    ()
                                              .toLowerCase();
            sTempClassName = sTempClassName.substring
                   ( sTempClassName.lastIndexOf( '.' ) + 1 );
            _sFullIniFileName = _sApplicationPath + File.separator +
                   sTempClassName + S_INI_SUFFIX;
            Util.debug( this
                        , "<init>(..)._sFullIniFileName: " 
                          + _sFullIniFileName );
            if (FileUtil.existsFile(_sFullIniFileName)) 
            {
                _sApplicationName = Util.firstCharToUpperCase(sTempClassName);
                _bSurpressInifileOption = true;
            }
            else 
            {
                _sFullIniFileName = _sApplicationPath + File.separator +
                       _sApplicationName.toLowerCase() +
                       S_INI_SUFFIX;
                // hack ahead!!!
                // find /project/src/my/package/app.ini
                if ( !FileUtil.existsFile( _sFullIniFileName ) ) 
                {
                    int classesIndex = _sFullIniFileName.lastIndexOf
                           ( File.separator + "classes" +
                             File.separator );
                    String sNewFullIniFileName = _sFullIniFileName.
                           substring( 0, classesIndex + 1 ) +
                           "src" + File.separator +
                           sPackageName.replace( '.', File.separatorChar ) +
                           File.separator + _sApplicationName.toLowerCase() +
                           S_INI_SUFFIX;
                    Util.debug( this, "<init>(..).sNewFullIniFileName: " +
                                sNewFullIniFileName );
                    if ( FileUtil.existsFile( sNewFullIniFileName ) ) 
                    {
                        _sFullIniFileName = sNewFullIniFileName;
                    }
                }
            }
        } 
        else 
        {
            _sFullIniFileName = FileUtil.getAbsoluteFileName(_sFullIniFileName);
            if (Util.isEmpty(_sFullIniFileName)) 
            {
                Util.println("Error: No ini file name specified.");
                pExitable_.setExit();

                return;
            }
        }
        Util.debug("Init.<init>(..)._sFullIniFileName: " + _sFullIniFileName);
        
        // init colors and stuff
        _init();

        // fills options and arguments data structures
        _processParameters( pExitable_, asArg_ );
    }

    /**
     * Set debug flag in case '-debug' is part of the options.
     * This method gets used because we want to immediately turn
     * debug on before all standard args are processed.
     */
    private void _setDebug( String[] asArg_ )
    {
        if ( asArg_ != null ) 
        {
            for( int option = 0; option < asArg_.length; option++ ) 
            {
                if ( asArg_[ option ].equals( "-debug" ) ) 
                {
                    Util.setDebug( true );
                }
            }
        }
    }

    /**
     * Fills options and arguments data structures.
     */
    private void _processParameters( Exitable pExitable_
                                     , String[] asArg_ )
    {
        for(int nr = 0; nr < asArg_.length; nr++) 
        {
            if (asArg_[nr].charAt(0) != '-') 
            {
                _vArguments.addElement(new String(asArg_[nr]));
            }
            else
            {
                String sOption = asArg_[nr].substring(1, asArg_[nr].length());
                Util.debug("Init.<init>(..).sOption: " + sOption);
                if (sOption.equals("version")) 
                {
                    Util.println( "" + _sApplicationName.toUpperCase() +
                                  " version " + getVersionString() +
                                  " " + getVersionDate() +
                                  " by " + getAuthor() );
                    pExitable_.setExit();
                }
                else if (sOption.equals("help")) 
                {
                    printHelpMessage();
                    pExitable_.setExit();

                    return;
                } 
                else if (sOption.equals("inifile")) 
                {
                    nr++;
                    if (_bSurpressInifileOption) 
                    {
                        Util.println("Error: No custom ini file supported!");
                        pExitable_.setExit();
                        
                        return;
                    }
                } 
                else if (sOption.equals("debug")) 
                {
                    // debug mode already set above
                    /*Util.setDebug(true); */
                }
                else 
                {
                    // user option
                    String sOptionDocu = getKeyValue(S_INIFILE_HELP,
                                                     sOption);
                    // Does the option really exists?
                    if (!Util.isEmpty(sOptionDocu)) 
                    {
                        Option pOption = new Option(sOption, sOptionDocu);
                        Object oOptionValue = new Boolean(true);
                        if (pOption.getType() == Option.STRING) 
                        {
                            if (nr + 1 >= asArg_.length) 
                            {
                                Util.println( "Error: Option " 
                                              + sOption 
                                              + " needs a string value." );
                                printHelpMessage();
                                pExitable_.setExit();

                                return;
                            }
                            oOptionValue = new String(asArg_[nr + 1]);
                            nr++;
                        }
                        _htOptions.put(sOption, oOptionValue);
                    } 
                    else 
                    {
                        Util.println( "Error: Option '" 
                                      + sOption
                                      + "' is not acceptable." );
                        printHelpMessage();
                        pExitable_.setExit();

                        return;
                    }
                }
            }
        }
    }
    
    /**
     * The ini file will be first looked for in package/exitable.ini
     * and if not found in package/package.ini.
     *
     * @param   pExitable_     the controlling application object.
     * @param   asArg_         the original command line arguments forwarded
     *                         from the main method.
     * @param   sInfoHeader_   an RCS Header string from the application 
     *                         Main class.
     */
    public Init( Exitable pExitable_
                 , String[] asArg_
                 , String sInfoHeader_ )
    {
        this(pExitable_, asArg_, sInfoHeader_, null);
    }

    /**
     * Used only by the Init.clone method.
     */
    protected Init() 
    {
        super();
    }

    public String getHelpMessage()
    {
        String sOptions = getKeyValue( S_INIFILE_HELP, "Options" );
        Vector vOptions = Util.stringToLines( sOptions, ',' );
        // htOptions contains Option objects
        Hashtable htOptions = new Hashtable();
        // to format the help messages
        int maxOptionLength = 7;
        for(Enumeration eOptions = vOptions.elements();
            eOptions.hasMoreElements(); )
        {
            String sNextOption = (String) eOptions.nextElement();
            Util.debug( this
                        , "printHelpMessage().sNextOption: " 
                          + sNextOption );
            Option pOption = new Option(sNextOption,
                                        getKeyValue(S_INIFILE_HELP,
                                                    sNextOption));
            
            htOptions.put(sNextOption, pOption);
            
            if (sNextOption.length() > maxOptionLength) 
            {
                maxOptionLength = sNextOption.length();
            }
        }
        
        String sUsage = getKeyValue(S_INIFILE_HELP,
                                    S_INIFILE_HELP_USAGE);
        String sSynopsis = "Usage: " + _sApplicationName.toLowerCase() + " ";
        String sMandatory = "";
        String sOptional = "";
        String sRemarks = "";
        for(Enumeration eOptions = vOptions.elements();
            eOptions.hasMoreElements(); )
        {
            String sOption = (String) eOptions.nextElement();
            Option optNext = (Option) htOptions.get(sOption);
            sRemarks +=  "       " + sOption + ":   " +
                   Util.getSpaces(maxOptionLength -
                                  sOption.length()) +
                   optNext.getDescription() + "\n";
            if (optNext.getType() == Option.STRING) 
            {
                sOption += S_OPTION_STRING;
            }
            if (optNext.isMandatory()) 
            {
                sMandatory += "-" + sOption + " ";
            } 
            else 
            {
                sOptional += "[-" + sOption + "] ";
            }
        }
        sRemarks += "       version:   " +
               Util.getSpaces(maxOptionLength - 7) +
               "Prints the version number of the program.\n" +
               "       help:   " +
               Util.getSpaces(maxOptionLength - 4) +
               "Prints this help message.\n" +
               "       debug:   " +
               Util.getSpaces(maxOptionLength - 5) +
               "Prints debugging information while running.";
        if (!_bSurpressInifileOption) 
        {
            sRemarks += "\n       inifile:   " +
                   Util.getSpaces(maxOptionLength - 7) +
                   "Starts this application with another ini file than\n" +
                   "           " +
                   Util.getSpaces(maxOptionLength) +
                   "the default one.";
        }
        sOptional += "[-version] [-help] [-debug] [-inifile foo.ini] ";
        sSynopsis += sMandatory + sOptional + sUsage;

        String sRetVal = sSynopsis
               + "\n"
               + sRemarks
               + "\n";
        
        return sRetVal;
    }
    
    public void printHelpMessage() 
    {
        Util.print( getHelpMessage() );
    }
   
    /**
     * @return true on error.
     */
    public synchronized boolean setKeyValue(String sKey_, int value_) 
    {
        return setKeyValue(sKey_, Util.itoa(value_));
    }
    
    /**
     * @return true on error.
     */
    public synchronized boolean setKeyValue(String sKey_,
                                            String sValue_)
    {
        return setKeyValue(S_INIFILE_INIT, sKey_, sValue_);
    }
    
    /**
     * @return true on error.
     */
    public synchronized boolean setKeyValue(String sSection_,
                                            String sKey_,
                                            String sValue_)
    {
        _hshKeyValues.put(sSection_ + sKey_, sValue_);
        IniFile.setKeyValue(_sFullIniFileName, sSection_,
                            sKey_, sValue_);
        return(IniFile.getStatus() != IniFile.OK);
    }
   
    public Vector getArguments() 
    {
        return (Vector) _vArguments.clone();
    }
   
    public Hashtable getOptions() 
    {
        return (Hashtable) _htOptions.clone();
    }
   
    public Enumeration getArgumentsElements() 
    {
        return(_vArguments.elements());
    }
   
    public synchronized String getKeyValue(String sSection_,
                                           String sKey_)
    {
        if (_hshKeyValues.containsKey(sSection_ + sKey_)) 
        {
            return (String) _hshKeyValues.get(sSection_ + sKey_);
        }
        /*Util.debug( this
                    , "getKeyValue(..)._sFullIniFileName: "
                      + _sFullIniFileName );
        Util.debug( this
                    , "getKeyValue(..).sSection_: "
                      + sSection_ );
        Util.debug( this
                    , "getKeyValue(..).sKey_: "
                      + sKey_ );
        Util.debug( this
                    , "getKeyValue(..)._sIniFileContent: "
                      + _sIniFileContent );*/
        String sValue = IniFile.getKeyValue(_sFullIniFileName,
                                            sSection_, sKey_,
                                            _sIniFileContent);
        Util.debug("Init.getKeyValue(..).IniFile.status(): " +
                   IniFile.getStatus());
        _hshKeyValues.put(sSection_ + sKey_, sValue);
        
        return sValue;
    }
   
    public synchronized String getKeyValue(String sKey_) 
    {
        return getKeyValue(S_INIFILE_INIT, sKey_);
    }
   
    /**
     * @see #getVersionString
     */
    public String getInfoHeader() 
    {
        return(_sInfoHeader);
    }
   
    public Color getBackground() 
    {
        if ( _bColorsInitialized == false )
        {
            _initColors();
        }
        if ( _clrBackground == null ) 
        {
            _clrBackground = _stringToColor( _sBackground );
        }
        
        return new Color( _clrBackground.getRGB() );
    }
   
    public Color getForeground() 
    {
        if ( _bColorsInitialized == false )
        {
            _initColors();
        }
        if ( _clrForeground == null ) 
        {
            _clrForeground = _stringToColor( _sForeground );
        }
        
        return new Color( _clrForeground.getRGB() );
    }
   
    public Color getListBackground() 
    {
        if ( _bColorsInitialized == false )
        {
            _initColors();
        }
        if ( _clrListBackground == null ) 
        {
            _clrListBackground = _stringToColor( _sListBackground );
        }
        
        return new Color( _clrListBackground.getRGB() );
    }
   
    public Color getListForeground() 
    {
        if ( _bColorsInitialized == false )
        {
            _initColors();
        }
        if ( _clrListForeground == null ) 
        {
            _clrListForeground = _stringToColor( _sListForeground );
        }
        
        return new Color( _clrListForeground.getRGB() );
    }

    public Color getListHighlight() 
    {
        if ( _bColorsInitialized == false )
        {
            _initColors();
        }
        if ( _clrListHighlight == null ) 
        {
            _clrListHighlight = _stringToColor( _sListHighlight );
        }
        
        return new Color( _clrListHighlight.getRGB() );
    }

    public String colorToString(Color pColor_) 
    {
        Util.panicIf(pColor_ == null);
        
        return("" + pColor_.getRed() + " " +
               pColor_.getGreen() + " " + pColor_.getBlue());
    }
   
    public void setBackground(Color pColor_) 
    {
        if ( _bColorsInitialized == false )
        {
            _initColors();
        }
        Util.panicIf(pColor_ == null);
        _clrBackground = new Color(pColor_.getRGB());
        setKeyValue(S_INIFILE_COLORS, S_INIFILE_COLORS_BACK,
                    colorToString(_clrBackground));
    }
   
    public void setListBackground(Color pColor_) 
    {
        if ( _bColorsInitialized == false )
        {
            _initColors();
        }
        Util.panicIf(pColor_ == null);
        _clrListBackground = new Color(pColor_.getRGB());
        setKeyValue(S_INIFILE_COLORS, S_INIFILE_COLORS_LISTBACK,
                    colorToString(_clrListBackground));
    }
   
    public void setListForeground(Color pColor_) 
    {
        if ( _bColorsInitialized == false )
        {
            _initColors();
        }
        Util.panicIf(pColor_ == null);
        _clrListForeground = new Color(pColor_.getRGB());
        setKeyValue(S_INIFILE_COLORS, S_INIFILE_COLORS_LISTFORE,
                    colorToString(_clrListForeground));
    }
    
    public void setListHighlight(Color pColor_) 
    {
        if ( _bColorsInitialized == false )
        {
            _initColors();
        }
        Util.panicIf(pColor_ == null);
        _clrListHighlight = new Color(pColor_.getRGB());
        setKeyValue(S_INIFILE_COLORS, S_INIFILE_COLORS_LISTHIGHLIGHT,
                    colorToString(_clrListHighlight));
   }
   
    /**
     * Set the application name explicitly.
     * The standard name is normally the name of the main
     * package.
     */
    public void setApplicationName( String sApplicationName_ ) 
    {
        _sApplicationName = sApplicationName_;
    }

    /**
     * The first char is upper case (like "Project").
     */
    public String getApplicationName() 
    {
        return _sApplicationName;
    }

    /**
     * @return   for example: "/home/clemens/src/java/jacob"
     */
    public String getApplicationPath() 
    {
        return _sApplicationPath;
    }

    /**
     * Return location of the help set documentation file
     * for this application.
     * The hs file should be located in the doc directory
     * with name applicationname.hs.
     *
     * @return   E.g. "file:/home/clemens/project/doc/appname.hs"
     */
    public String getHelpBrokerURL() 
    {
        String sPath = getApplicationPath();
        Util.debug( this, "getHelpBrokerURL(..).sPath: " +
                    sPath );
        sPath = Util.replace( sPath, '\\', '/' );
        if ( !Util.endsWith( sPath, "/" ) ) 
        {
            sPath += "/";
        }
        if ( Util.endsWith( sPath, "/lib/" ) ||
             sPath.equals( "lib/" ) )
        {
            sPath += "../";
        }
        else 
        {
            int classesIndex = sPath.lastIndexOf( "/classes/" );
            if ( classesIndex == -1 ) 
            {
                if ( sPath.startsWith( "classes/" ) ) 
                {
                    classesIndex = 0;
                }
            } 
            else 
            {
                classesIndex++;
            }
            if ( classesIndex != -1 ) 
            {
                sPath = sPath.substring( 0, classesIndex );
            }
        }
        
        sPath = FileUtil.getAbsoluteFileName( sPath );
        Util.debug( this, "getHelpBrokerURL(..).sPath: " +
                    sPath );
        String sHSFile =   "file:" 
                         + sPath 
                         + "/doc/"
                         + getApplicationName()
                           .toLowerCase() 
                         + ".hs";
        
        return sHSFile;
    }

    /**
     * Return location of doc directory for this application.
     *
     * @return   E.g. "file:/home/clemens/ccl/doc"
     */
    public String getApplicationDocPath() 
    {
        String sPath = getApplicationPath();
        sPath = Util.replace( sPath, '\\', '/' );
        if ( !Util.endsWith( sPath, "/" ) ) 
        {
            sPath += "/";
        }
        if ( Util.endsWith( sPath, "/lib/" ) ||
             sPath.equals( "lib/" ) )
        {
            sPath += "../";
        } 
        else 
        {
            int classesIndex = sPath.lastIndexOf( "/classes/" );
            if ( classesIndex == -1 ) 
            {
                if ( sPath.startsWith( "classes/" ) ) 
                {
                    classesIndex = 0;
                }
            } 
            else 
            {
                classesIndex++;
            }
            if ( classesIndex != -1 ) 
            {
                sPath = sPath.substring( 0, classesIndex );
            }
        }
        
        sPath = FileUtil.getAbsoluteFileName( sPath );
        sPath = FileUtil.concatPath( sPath, "doc" );

        return sPath;
    }

    private void _saveOldFiles() 
    {
        // Alte Files speichern
        String sOldFiles = "";
        for (int i = 0; i < _vOldFiles.size(); i++) 
        {
            FileObject pFileObject = (FileObject) _vOldFiles.elementAt(i);
            Util.debug( "Init._saveOldFiles()._vOldFiles: " +
                        _vOldFiles );
            if ( Util.equalsCaseless(pFileObject.getName(),
                                     getFileName()) )
            {
                _vOldFiles.removeElementAt(i);
                i--;
            } 
            else 
            {
                sOldFiles += pFileObject.getFileFullName() + ";";
            }
        }
        if (Util.getOccurances(sOldFiles, 59) > _maxProjects) 
        {
            sOldFiles = sOldFiles.substring( sOldFiles.indexOf(59) + 1
                                             , sOldFiles.length() );
            _vOldFiles.removeElementAt(0);
        }
        Util.debug( "Init._saveOldFiles().sOldFiles: " + sOldFiles );
        setKeyValue(S_INIFILE_INIT_OLDFILES, sOldFiles);
    }

    /**
     * For File->New use "" or null for sFileFullName_.
     *
     * @return   IniFile status.
     */
    public boolean setFileFullName(String sFileFullName_) 
    {
        if ( !Util.isEmpty( getFileFullName() ) ) 
        {
           makeThisFileOld();
        }

        if ( Util.isEmpty( sFileFullName_ ) ) 
        {
           _sFileFullName = "";
           setKeyValue( S_INIFILE_INIT, S_INIFILE_INIT_FILE,
                        "" );
           _saveOldFiles();

           int status = IniFile.getStatus();
           boolean bRetVal = (status != IniFile.OK);
           
           return bRetVal;
        }

        // falls relativer Pfad -> absolut
        _sFileFullName = FileUtil.getAbsoluteFileName(sFileFullName_);
        Util.debug( this
                    , "setFileFullName(..)._sApplicationPath: " 
                      + _sApplicationPath );
        Util.debug( this
                    , "setFileFullName(..)._sApplicationName: " 
                      + _sApplicationName );
        setKeyValue(S_INIFILE_INIT,
                    S_INIFILE_INIT_FILE, _sFileFullName);
        int status = IniFile.getStatus();
        boolean bRetVal = (status != IniFile.OK);
        
        // Alte Files speichern
        Util.debug("Init: setFileFullName: _vOldFiles: " +
                   _vOldFiles);
        String sOldFiles = "";
        for (int i = 0; i < _vOldFiles.size(); i++) 
        {
            FileObject pFileObject = (FileObject) _vOldFiles.elementAt(i);
            if ( Util.equalsCaseless(pFileObject.getName(),
                                     getFileName()) )
            {
                Util.debug("Init: setFileFullName: pFileObject.getName(): " +
                           pFileObject.getName());
                Util.debug("Init.setFileFullName().getFileName(): " +
                           getFileName());
                _vOldFiles.removeElementAt(i);
                i--;
            } 
            else 
            {
                sOldFiles += pFileObject.getFileFullName() + ";";
            }
        }
        if (Util.getOccurances(sOldFiles, 59) > _maxProjects) 
        {
            sOldFiles = sOldFiles.substring( sOldFiles.indexOf(59) + 1
                                             , sOldFiles.length() );
            _vOldFiles.removeElementAt(0);
        }
        Util.debug("Init: setFileFullName: sOldFiles: " + sOldFiles);
        setKeyValue(S_INIFILE_INIT_OLDFILES, sOldFiles);
        
        return bRetVal;
    }
        
    /**
     * First char is upper case, the rest lower case. Only the name
     * is returned, no path or file postfix information is given.
     *
     * @see #getFileFullName
     * @see #getFilePath
     */
    public String getFileName() 
    {
        // \ zu / umtauschen
        Util.debug("Init: getFileName: _sFileFullName: " + _sFileFullName);
        String sRetVal = new String(_sFileFullName);
        sRetVal = sRetVal.replace('\\', '/');
      
        // String zwische letztem / und .
        int indexStart = sRetVal.lastIndexOf('/') + 1;
        int indexStop  = sRetVal.lastIndexOf('.');
        if (indexStart == -1 || indexStop == -1 || 
            indexStart > indexStop) 
        {
            return "";
        }
        sRetVal = sRetVal.substring(indexStart, indexStop);
        sRetVal = Util.firstCharToUpperCase(sRetVal);
        
        return sRetVal;
    }
   
    public String getFileFullName() 
    {
        return _sFileFullName;
    }
   
    /**
     * @return       File separator is '/' and the path ends with a file
     *               separator char.
     *
     * @see          #getProjectHome()
     * @deprecated
     */
    public String getFilePath() 
    {
        // \ zu / umtauschen
        String sRetVal = new String(_sFileFullName);
        sRetVal = sRetVal.replace('\\', '/');
        
        // String zwische letztem / und .
        int indexStop = sRetVal.lastIndexOf('/') + 1;
        Util.panicIf(indexStop == -1);
        sRetVal = sRetVal.substring(0, indexStop);
        
        return sRetVal;
    }
   
    /**
     * Returns the directory of the current projects file.
     */
    public String getProjectHome() 
    {
        return FileUtil.getAbsoluteFileName
               ( new File( getFileFullName() ).getParent() );
    }
   
    public Enumeration getOldFilesElements() 
    {
        return _vOldFiles.elements();
    }
   
    public int getOldFilesSize() 
    {
        return _vOldFiles.size();
    }
   
    public String getOldFileFullName(String string) 
    {
        for (Enumeration enumeration = _vOldFiles.elements();
             enumeration.hasMoreElements(); )
        {
            FileObject pFileObject = (FileObject) enumeration.nextElement();
            if (Util.equalsCaseless(pFileObject.getName(), string))
            {
                return pFileObject.getFileFullName();
            }
        }
        Util.panicIf( true
                      , "ccl.util.Init.getOldFileFullName(.)"
                        + ".A file should always have been found!" );
        
        return null;
    }
   
    /**
     * This method stores the actual file in the old file vector,
     * no saving or anything else like removing double fils.
     */
    public void makeThisFileOld() 
    {
        if (_sFileFullName != null && (!_sFileFullName.equals(""))) 
        {
            Util.debug( "Init.makeThisFileOld()._sFileFullName: " +
                        _sFileFullName );
            FileObject pFileObject = new FileObject( _sFileFullName );
            Util.debug( "Init.makeThisFileOld().pFileObject: " +
                        pFileObject );
            _vOldFiles.addElement( pFileObject );
            Util.debug("Init.makeThisFileOld()._vOldFiles: " +
                       _vOldFiles);
        }
    }
   
    /*public Language getLanguage() {
      return _pLanguage;
      }*/
   
    public String getVersionString() 
    {
        Util.debug( this, "getVersionString()._sInfoHeader: " + _sInfoHeader );
        if (_sInfoHeader == null) 
        {
            return "1.1";
        }
        int index = _sInfoHeader.indexOf(" ", 9) + 1;
        String sHead = _sInfoHeader.substring(index, _sInfoHeader.length());
        index = sHead.indexOf(' ');
        String sVersion = sHead.substring(0, index);
        Util.debug( this, "getVersionString().sVersion: " + sVersion );
        
        return sVersion;
    }
   
    public int getVersion() 
    {
        String sVersion = getVersionString();
        sVersion = sVersion.substring(0, sVersion.indexOf('.'));
        
        return Util.atoi(sVersion);
    }
   
    public int getRelease() 
    {
        String sVersion = getVersionString();
        String sRelease = sVersion.substring(sVersion.indexOf('.') + 1,
                                             sVersion.length());
        
        return Util.atoi(sRelease);
    }
   
    /**
     * @return The right language is automaticly used.
     */
    public String getVersionDate() 
    {
        String sVersion = getVersionString();
        int index = _sInfoHeader.indexOf(sVersion) + sVersion.length() + 1;
        String sHead = _sInfoHeader.substring(index, _sInfoHeader.length());
        index = sHead.indexOf(' ');
        String sDate = sHead.substring(0, index);
        
        // Format Date
        int year = Util.atoi(sDate.substring(0, 4));
        int month = Util.atoi(sDate.substring(5, 7)) - 1;
        int day = Util.atoi(sDate.substring(8, 10));
        /*MultiDate pMultiDate = new MultiDate(year, month, day,
          getLanguage());
          sDate = pMultiDate.toLocaleDateString();*/
        sDate = "" + year + "-" + Util.paddWithZero
               ( month + 1, 2 ) + "-" + Util.paddWithZero( day, 2 );
        
        return sDate;
    }
    
    public String getVersionTime() 
    {
        String sVersion = getVersionString();
        int index = _sInfoHeader.indexOf(sVersion) + sVersion.length() + 12;
        String sHead = _sInfoHeader.substring(index, _sInfoHeader.length());
        index = sHead.indexOf(' ');
        String sTime = sHead.substring(0, index);
      
        return sTime;
    }

    public static String getProjectSuffixKey() 
    {
        return S_PROJECTSUFFIX_KEY;
    }

    public String getAuthor() 
    {
        return _sAuthor;
    }

    public void setAuthor( String sAuthor_ ) 
    {
        _sAuthor = sAuthor_;
    }

    /**
     * Create a backup of the ini file.
     *
     * @return   Error message in case something went wrong, 
     *           null otherwise.
     */
    public String createBackupFile() 
    {
        String sErrorMessage = null;
        if ( _sFullIniFileName == null ) 
        {
            return sErrorMessage;
        }

        try
        {
            FileUtil.createBackupFile( _sFullIniFileName );
        }
        catch( IOException exception ) 
        {
            sErrorMessage = "Failure to create backup for initialization file\n"
                   + "'"
                   + _sFullIniFileName
                   + "':\n"
                   + exception;
        }

        return sErrorMessage;
    }

    /**
     * Return a copy of this object.
     */
    public Object clone() 
    {
        Init iniRetVal = new Init();

        iniRetVal._maxProjects      = this._maxProjects;
        iniRetVal._sInfoHeader       = this._sInfoHeader;
        iniRetVal._clrBackground     = this.getBackground();
        iniRetVal._clrForeground     = this.getForeground();
        iniRetVal._clrListBackground = this.getListBackground();
        iniRetVal._clrListForeground = this.getListForeground();
        iniRetVal._clrListHighlight  = this.getListHighlight();
        iniRetVal._clrTextBackground = null;
        iniRetVal._clrTextForeground = null;
        iniRetVal._sAuthor           = this._sAuthor;
        iniRetVal._sFileFullName     = this._sFileFullName;
        iniRetVal._sApplicationName  = this._sApplicationName;
        iniRetVal._sApplicationPath  = this._sApplicationPath;
        iniRetVal._sFullIniFileName  = this._sFullIniFileName;
        iniRetVal._htOptions         = (Hashtable) this._htOptions .clone();
        iniRetVal._vArguments        = (Vector)    this._vArguments.clone();
        iniRetVal._vOldFiles         = (Vector)    this._vOldFiles .clone();
        iniRetVal._bSurpressInifileOption = this._bSurpressInifileOption;
        iniRetVal._sIniFileContent        = this._sIniFileContent;

        return iniRetVal;
    }
}
