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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * @author   Chr. Clemens Lee, recursive feature by Pääkö Hannu,
 *           additional javadoc metrics by Emilio Gongora, &lt;emilio@sms.nl&gt;,
 *           and Guillermo Rodriguez, &lt;guille@sms.nl&gt;.
 */
public class Javancss
{

	private JavaParser _pJavaParser = null;
	private Vector _vJavaSourceFiles = new Vector();
	private String _sErrorMessage = null;
	private Vector _vMethodComplexities = new Vector();
	private Hashtable _htProcessedAtFiles = new Hashtable();

	public Javancss(String sJavaSourceFile_)
	{
		//System.out.println("Javancss.<init>(String).sJavaSourceFile_: " + sJavaSourceFile_);
		_sErrorMessage = null;
		_vJavaSourceFiles = new Vector();
		_vJavaSourceFiles.addElement(sJavaSourceFile_);
		try
		{
			_measureFiles(_vJavaSourceFiles);
		}
		catch (Exception e)
		{
			System.out.println("Javancss.<init>(String).e: " + e);
		}
		catch (TokenMgrError pError)
		{
			System.out.println("Javancss.<init>(String).pError: " + pError);
		}
	}

	private void _measureFiles(Vector vJavaSourceFiles_) throws IOException, ParseException,
			TokenMgrError
	{
		// for each file
		for (Enumeration e = vJavaSourceFiles_.elements(); e.hasMoreElements();)
		{
			String sJavaFileName = (String)e.nextElement();

			// if the file specifies other files...
			if (sJavaFileName.charAt(0) == '@')
			{
				if (sJavaFileName.length() > 1)
				{
					String sFileName = sJavaFileName.substring(1);
					sFileName = FileUtil.normalizeFileName(sFileName);
					if (_htProcessedAtFiles.get(sFileName) != null)
					{
						continue;
					}
					_htProcessedAtFiles.put(sFileName, sFileName);
					String sJavaSourceFileNames = null;
					try
					{
						sJavaSourceFileNames = FileUtil.readFile(sFileName);
					}
					catch (IOException pIOException)
					{
						_sErrorMessage = "File Read Error: " + sFileName;

						throw pIOException;
					}
					Vector vTheseJavaSourceFiles = Util.stringToLines(sJavaSourceFileNames);
					_measureFiles(vTheseJavaSourceFiles);
				}
			}
			else
			{
				try
				{
					_measureSource(sJavaFileName);
				}
				catch (Throwable pThrowable)
				{
					// hmm, do nothing? Use getLastError() or so to check for details.
				}
			}
		}
	}

	private void _measureSource(String sSourceFileName_) throws IOException, ParseException,
			TokenMgrError
	{
		// take user.dir property in account
		sSourceFileName_ = FileUtil.normalizeFileName(sSourceFileName_);

		DataInputStream disSource = null;

		// opens the file
		try
		{
			disSource = new DataInputStream(new FileInputStream(sSourceFileName_));
		}
		catch (IOException pIOException)
		{
			if (_sErrorMessage == null)
			{
				_sErrorMessage = "";
			}
			else
			{
				_sErrorMessage += "\n";
			}
			_sErrorMessage += "File not found: " + sSourceFileName_;

			throw pIOException;
		}

		String sTempErrorMessage = _sErrorMessage;
		try
		{
			// the same method but with a DataInputSream
			_measureSource(disSource);
		}
		catch (ParseException pParseException)
		{
			if (sTempErrorMessage == null)
			{
				sTempErrorMessage = "";
			}
			sTempErrorMessage += "ParseException in " + sSourceFileName_
					+ "\nLast useful checkpoint: \"" + _pJavaParser.getLastFunction() + "\"\n";
			sTempErrorMessage += pParseException.getMessage() + "\n";

			_sErrorMessage = sTempErrorMessage;

			throw pParseException;
		}
		catch (TokenMgrError pTokenMgrError)
		{
			if (sTempErrorMessage == null)
			{
				sTempErrorMessage = "";
			}
			sTempErrorMessage += "TokenMgrError in " + sSourceFileName_ + "\n"
					+ pTokenMgrError.getMessage() + "\n";
			_sErrorMessage = sTempErrorMessage;

			throw pTokenMgrError;
		}
	}

	private void _measureSource(DataInputStream disSource_) throws ParseException, TokenMgrError
	{
		try
		{
			// create a parser object
			_pJavaParser = new JavaParser(disSource_);
			// execute the parser
			_pJavaParser.compilationUnit();
			//System.out.println("Javancss._measureSource(DataInputStream).SUCCESSFULLY_PARSED");
			// add new data to global vector
			_vMethodComplexities.addAll(_pJavaParser.getMethodComplexities());
		}
		catch (ParseException pParseException)
		{
			if (_sErrorMessage == null)
			{
				_sErrorMessage = "";
			}
			_sErrorMessage += "ParseException in STDIN";
			if (_pJavaParser != null)
			{
				_sErrorMessage += "\nLast useful checkpoint: \"" + _pJavaParser.getLastFunction()
						+ "\"\n";
			}
			_sErrorMessage += pParseException.getMessage() + "\n";

			throw pParseException;
		}
		catch (TokenMgrError pTokenMgrError)
		{
			if (_sErrorMessage == null)
			{
				_sErrorMessage = "";
			}
			_sErrorMessage += "TokenMgrError in STDIN\n";
			_sErrorMessage += pTokenMgrError.getMessage() + "\n";

			throw pTokenMgrError;
		}
	}

	public Vector getMethodComplexities()
	{
		return (_vMethodComplexities);
	}

}
