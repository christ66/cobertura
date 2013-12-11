package net.sourceforge.cobertura.metrics.model;

import net.sourceforge.cobertura.metrics.model.Namespace;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Scope definition for filtering/ordering SourceLocations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
@XmlType(namespace = Namespace.COBERTURA_NAMESPACE)
@XmlEnum
public enum LocationScope {

    /**
     * Full project scope, indicating all SourceLocations within the project.
     */
    PROJECT,

    /**
     * Package scope, indicating all SourceLocations within a given package (not including subpackages).
     */
    PACKAGE,

    /**
     * Class scope, indicating all SourceLocations within a supplied Class.
     * Inner classes are not included within the scope of their enclosing Class.
     */
    CLASS,

    /**
     * Method scope, indicating all SourceLocations within a supplied Method.
     * Closures are not included within the scope of their enclosing Method.
     */
    METHOD,

    /**
     * Line scope, indicating all SourceLocations within a supplied Line.
     */
    LINE,

    /**
     * Segment scope (atomic), indicating the SourceLocation of a supplied branch segment within a given Line.
     */
    SEGMENT
}
