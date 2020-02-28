package net.sourceforge.cobertura.javancss.parser;

import java.util.*;

import net.sourceforge.cobertura.javancss.FunctionMetric;
import net.sourceforge.cobertura.javancss.ObjectMetric;
import net.sourceforge.cobertura.javancss.PackageMetric;

public interface JavaParserInterface
{
    public void parse()
        throws Exception;

    public void parseImportUnit()
        throws Exception;

    public int getNcss();

    public int getLOC();

    // added by SMS
    public int getJvdc();

    /*public int getTopLevelClasses() {
      return _topLevelClasses;
      }*/

    public List<FunctionMetric> getFunction();

    /**
     * @return Top level classes in sorted order
     */
    public List<ObjectMetric> getObject();

    /**
     * @return The empty package consists of the name ".".
     */
    public Map<String, PackageMetric> getPackage();

    public List<Object[]> getImports();

    /**
     * name, beginLine, ...
     */
    public Object[] getPackageObjects();

    /**
     * if javancss is used with cat *.java a long
     * input stream might get generated, so line
     * number information in case of an parse exception
     * is not very useful.
     */
    public String getLastFunction();
}
