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

package net.sourceforge.cobertura.javancss;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * While the Java parser class might be the heart of JavaNCSS,
 * this class is the brain. This class controls input and output and
 * invokes the Java parser.
 *
 * @author    Chr. Clemens Lee <clemens@kclee.com>
 *            , recursive feature by Pääkö Hannu
 *            , additional javadoc metrics by Emilio Gongora <emilio@sms.nl>
 *            , and Guillermo Rodriguez <guille@sms.nl>.
 * @version   $Id$
 */
public class Javancss implements JavancssConstants
{
    static final int LEN_NR = 3;
    static final String S_INIT__FILE_CONTENT =
        "[Init]\n" +
        "Author=Chr. Clemens Lee\n" +
        "\n" +
        "[Help]\n"+
        "; Please do not edit the Help section\n"+
        "HelpUsage=@srcfiles.txt | *.java | <stdin>\n" +
        "Options=ncss,package,object,function,all,gui,xml,out,recursive,check\n" +
        "ncss=b,o,Counts the program NCSS (default).\n" +
        "package=b,o,Assembles a statistic on package level.\n" +
        "object=b,o,Counts the object NCSS.\n" +
        "function=b,o,Counts the function NCSS.\n" +
        "all=b,o,The same as '-function -object -package'.\n" +
        "gui=b,o,Opens a gui to present the '-all' output in tabbed panels.\n" +
        "xml=b,o,Output in xml format.\n" +
        "out=s,o,Output file name. By default output goes to standard out.\n"+
        "recursive=b,o,Recurse to subdirs.\n" +
        "check=b,o,Triggers a javancss self test.\n" +
        "\n" +
        "[Colors]\n" +
        "UseSystemColors=true\n";
    
    private int _ncss = 0;
    private int _loc = 0;
    private JavaParser _pJavaParser = null;
    private Vector _vJavaSourceFiles = new Vector();
    private String _sErrorMessage = null;
    private Throwable _thrwError = null;
    private Vector _vFunctionMetrics = new Vector();
    private Vector _vObjectMetrics = new Vector();
    private Vector _vPackageMetrics = null;
    private Vector _vImports = null;
    private Hashtable _htPackages = null;
    private Hashtable _htProcessedAtFiles = new Hashtable();
    private Object[] _aoPackage = null;

    /**
     * Just used for parseImports.
     */
    private String _sJavaSourceFileName = null;

    private DataInputStream createInputStream( String sSourceFileName_ )
    {
        DataInputStream disSource = null;

        try {
            disSource = new DataInputStream
                   (new FileInputStream(sSourceFileName_));
        } catch(IOException pIOException) {
            if ( Util.isEmpty( _sErrorMessage ) )
            {
                _sErrorMessage = "";
            }
            else
            {
                _sErrorMessage += "\n";
            }
            _sErrorMessage += "File not found: " + sSourceFileName_;
            _thrwError = pIOException;

            return null;
        }

        return disSource;
    }

    private void _measureSource(String sSourceFileName_)
        throws IOException,
               ParseException,
               TokenMgrError
    {
        // take user.dir property in account
        sSourceFileName_ = FileUtil.normalizeFileName( sSourceFileName_ );

        DataInputStream disSource = null;

        // opens the file
        try 
        {
            disSource = new DataInputStream
                   (new FileInputStream(sSourceFileName_));
        }
        catch(IOException pIOException) 
        {
            if ( Util.isEmpty( _sErrorMessage ) )
            {
                _sErrorMessage = "";
            }
            else
            {
                _sErrorMessage += "\n";
            }
            _sErrorMessage += "File not found: " + sSourceFileName_;
            _thrwError = pIOException;

            throw pIOException;
        }

        String sTempErrorMessage = _sErrorMessage;
        try {
            // the same method but with a DataInputSream
            _measureSource(disSource);
        } catch(ParseException pParseException) {
            if (sTempErrorMessage == null) {
                sTempErrorMessage = "";
            }
            sTempErrorMessage += "ParseException in " + sSourceFileName_ + 
                   "\nLast useful checkpoint: \"" + _pJavaParser.getLastFunction() + "\"\n";
            sTempErrorMessage += pParseException.getMessage() + "\n";
            
            _sErrorMessage = sTempErrorMessage;
            _thrwError = pParseException;
            
            throw pParseException;
        } catch(TokenMgrError pTokenMgrError) {
            if (sTempErrorMessage == null) {
                sTempErrorMessage = "";
            }
            sTempErrorMessage += "TokenMgrError in " + sSourceFileName_ + 
                   "\n" + pTokenMgrError.getMessage() + "\n";
            _sErrorMessage = sTempErrorMessage;
            _thrwError = pTokenMgrError;
            
            throw pTokenMgrError;
        }
    }

    private void _measureSource(DataInputStream disSource_)
        throws IOException,
               ParseException,
               TokenMgrError
    {
        try {
            // create a parser object
            _pJavaParser = new JavaParser(disSource_);
            // execute the parser
            _pJavaParser.CompilationUnit();
            _ncss += _pJavaParser.getNcss();       // increment the ncss
            _loc  += _pJavaParser.getLOC();        // and loc
            // add new data to global vector
            _vFunctionMetrics.addAll(_pJavaParser.getFunction());
            _vObjectMetrics.addAll(_pJavaParser.getObject());
            Hashtable htNewPackages = _pJavaParser.getPackage();
            /*Vector vNewPackages = new Vector();*/
            for(Enumeration ePackages = htNewPackages.keys();
                ePackages.hasMoreElements(); )
            {
                String sPackage = (String)ePackages.nextElement();
                PackageMetric pckmNext = (PackageMetric)htNewPackages.
                       get(sPackage);
                pckmNext.name = sPackage;
                PackageMetric pckmPrevious =
                       (PackageMetric)_htPackages.get
                       (sPackage);
                pckmNext.add(pckmPrevious);
                _htPackages.put(sPackage, pckmNext);
            }
        } catch(ParseException pParseException) {
            if (_sErrorMessage == null) {
                _sErrorMessage = "";
            }
            _sErrorMessage += "ParseException in STDIN";
            if (_pJavaParser != null) {
                _sErrorMessage += "\nLast useful checkpoint: \"" + _pJavaParser.getLastFunction() + "\"\n";
            }
            _sErrorMessage += pParseException.getMessage() + "\n";
            _thrwError = pParseException;
            
            throw pParseException;
        } catch(TokenMgrError pTokenMgrError) {
            if (_sErrorMessage == null) {
                _sErrorMessage = "";
            }
            _sErrorMessage += "TokenMgrError in STDIN\n";
            _sErrorMessage += pTokenMgrError.getMessage() + "\n";
            _thrwError = pTokenMgrError;
            
            throw pTokenMgrError;
        }
    }

    private void _measureFiles(Vector vJavaSourceFiles_)
        throws IOException,
               ParseException,
               TokenMgrError
    {
        // for each file
        for(Enumeration e = vJavaSourceFiles_.elements(); e.hasMoreElements(); ) 
        {
            String sJavaFileName = (String)e.nextElement();

            // if the file specifies other files...
            if (sJavaFileName.charAt(0) == '@') 
            {
                if (sJavaFileName.length() > 1) 
                {
                    String sFileName = sJavaFileName.substring(1);
                    sFileName = FileUtil.normalizeFileName( sFileName );
                    if (_htProcessedAtFiles.get(sFileName) != null) 
                    {
                        continue;
                    }
                    _htProcessedAtFiles.put( sFileName, sFileName );
                    String sJavaSourceFileNames = null;
                    try 
                    {
                        sJavaSourceFileNames = FileUtil.readFile(sFileName);
                    }
                    catch(IOException pIOException) 
                    {
                        _sErrorMessage = "File Read Error: " + sFileName;
                        _thrwError = pIOException;
                        
                        throw pIOException;
                    }
                    Vector vTheseJavaSourceFiles =
                           Util.stringToLines(sJavaSourceFileNames);
                    _measureFiles(vTheseJavaSourceFiles);
                }
            } 
            else 
            {
                try 
                {
                    _measureSource( sJavaFileName );
                } catch( Throwable pThrowable ) 
                {
                    // hmm, do nothing? Use getLastError() or so to check for details.
                }
            }
        }
    }

    /**
     * If arguments were provided, they are used, otherwise
     * the input stream is used.
     */
    private void _measureRoot(InputStream pInputStream_)
        throws IOException,
               ParseException,
               TokenMgrError
    {
        _htPackages = new Hashtable();
        
        // either there are argument files, or stdin is used
        if (_vJavaSourceFiles.size() == 0) {
            DataInputStream disJava = new java.io.DataInputStream(pInputStream_);
            _measureSource(disJava);
        } else {
            // the collection of files get measured
            _measureFiles(_vJavaSourceFiles);
        }
        
        _vPackageMetrics = new Vector();
        for(Enumeration ePackages = _htPackages.keys();
            ePackages.hasMoreElements(); )
        {
            String sPackage = (String)ePackages.nextElement();
            PackageMetric pckmNext = (PackageMetric)_htPackages.
                   get(sPackage);
            _vPackageMetrics.addElement(pckmNext);
        }
    }

    public Vector getImports() {
        return _vImports;
    }

    /**
     * Return info about package statement.
     * First element has name of package,
     * then begin of line, etc.
     */
    public Object[] getPackage() {
        return _aoPackage;
    }

    /**
     * The same as getFunctionMetrics?!
     */
    public Vector getFunctions() {
        return _vFunctionMetrics;
    }

    public Javancss(Vector vJavaSourceFiles_) {
        _vJavaSourceFiles = vJavaSourceFiles_;
        try {
            _measureRoot(System.in);
        } catch(Exception e) {
        } catch(TokenMgrError pError) {
        }
    }

    public Javancss(String sJavaSourceFile_) {
        _sErrorMessage = null;
        _vJavaSourceFiles = new Vector();
        _vJavaSourceFiles.addElement(sJavaSourceFile_);
        try {
            _measureRoot(System.in);
        } catch(Exception e) {
        	System.out.println( "Javancss.<init>(String).e: " + e );
        } catch(TokenMgrError pError) {
        	System.out.println( "Javancss.<init>(String).pError: " + pError );
        }
    }

    /**
     * Only way to create object that does not immediately
     * start to parse.
     */
    public Javancss() {
        super();

        _sErrorMessage = null;
        _thrwError = null;
    }

    public boolean parseImports() {
        if ( Util.isEmpty( _sJavaSourceFileName ) ) {
        	System.out.println( "Javancss.parseImports().NO_FILE" );

            return true;
        }
        DataInputStream disSource = createInputStream
               ( _sJavaSourceFileName );
        if ( disSource == null ) {
        	System.out.println( "Javancss.parseImports().NO_DIS" );

            return true;
        }

        try {
            _pJavaParser = new JavaParser(disSource);
            _pJavaParser.ImportUnit();
            _vImports = _pJavaParser.getImports();
            _aoPackage = _pJavaParser.getPackageObjects();
        } catch(ParseException pParseException) {
        	System.out.println( "Javancss.parseImports().PARSE_EXCEPTION" );
            if (_sErrorMessage == null) {
                _sErrorMessage = "";
            }
            _sErrorMessage += "ParseException in STDIN";
            if (_pJavaParser != null) {
                _sErrorMessage += "\nLast useful checkpoint: \"" + _pJavaParser.getLastFunction() + "\"\n";
            }
            _sErrorMessage += pParseException.getMessage() + "\n";
            _thrwError = pParseException;

            return true;
        } catch(TokenMgrError pTokenMgrError) {
        	System.out.println( "Javancss.parseImports().TOKEN_ERROR" );
            if (_sErrorMessage == null) {
                _sErrorMessage = "";
            }
            _sErrorMessage += "TokenMgrError in STDIN\n";
            _sErrorMessage += pTokenMgrError.getMessage() + "\n";
            _thrwError = pTokenMgrError;

            return true;
        }

        return false;
    }

    public void setSourceFile( String sJavaSourceFile_ ) {
        _sJavaSourceFileName = sJavaSourceFile_;
        _vJavaSourceFiles = new Vector();
        _vJavaSourceFiles.addElement(sJavaSourceFile_);
    }

    public int getNcss() {
        return _ncss;
    }

    public int getLOC() {
        return _loc;
    }

    // added by SMS
    public int getJvdc() {
        return _pJavaParser.getJvdc();
    }

    /**
     * JDCL stands for javadoc coment lines (while jvdc stands
     * for number of javadoc comments).
     */
    public int getJdcl() {
        return JavaParserTokenManager._iFormalComments;
    }
    
    public int getSl() {
        return JavaParserTokenManager._iSingleComments;
    }
    
    public int getMl() {
        return JavaParserTokenManager._iMultiComments;
    }
    //

    public Vector getFunctionMetrics() {
        return(_vFunctionMetrics);
    }

    public Vector getObjectMetrics() {
        return(_vObjectMetrics);
    }

    /**
     * Returns list of packages in the form
     * PackageMetric objects.
     */
    public Vector getPackageMetrics() {
        return(_vPackageMetrics);
    }

    public String getLastErrorMessage() {
        if (_sErrorMessage == null) {
            return null;
        }
        return(new String(_sErrorMessage));
    }

    public Throwable getLastError() {
        return _thrwError;
    }

}
