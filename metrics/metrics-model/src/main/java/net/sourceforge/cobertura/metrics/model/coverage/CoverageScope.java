package net.sourceforge.cobertura.metrics.model.coverage;

import net.sourceforge.cobertura.metrics.model.LocationScope;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Specification for how to aggregate CoverageRecords into a sensibly
 * sized scope, typically used for generating reports or validation criteria.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface CoverageScope extends Serializable, Comparable<CoverageScope> {

    /**
     * Path element separator.
     */
    String SEPARATOR = "/";

    /**
     * Retrieves a Pattern for the supplied LocationScope; the Pattern is used
     * to identify all SourceLocations within this CoverageScope.
     *
     * @param scope The LocationScope for which the identifying Pattern should be acquired.
     * @return an identifying Pattern for the supplied LocationScope, or {@code null} if no
     * Pattern was found for this CoverageScope.
     */
    Pattern get(LocationScope scope);

    /**
     * Retrieves the LocationScope of this CoverageScope, implying the smallest
     * syntactic scope for which this CoverageScope has Pattern definitions.
     * As an example, if a CoverageScope is of scope {@code LocationScope.CLASS},
     * then the {@code getPatterns() } method must have non-null Pattern values for
     * all LocationScopes down to the LocationScope.CLASS.
     *
     * @return The lowest scope for which this CoverageScope has a pattern definition.
     */
    LocationScope getLowestScope();

    /**
     * @return The human-readable name for this CoverageScope, such as the
     * fully qualified package or class name.
     */
    String getName();
}
