package net.sourceforge.cobertura.metrics.api.location;

import net.sourceforge.cobertura.metrics.model.location.SourceLocation;
import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Filter separating valid from invalid SourceLocation instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class SourceLocationFilter implements Filter<SourceLocation> {

    // Internal state
    private LocationScope scope;
    private boolean acceptAllInProject;
    private SortedMap<LocationScope, Pattern> locationPatternMap;

    public SourceLocationFilter() {
        setupInternalState(LocationScope.PROJECT);
    }

    public SourceLocationFilter(final String packagePattern) {

        // Assign internal state
        setupInternalState(LocationScope.PACKAGE);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
    }

    public SourceLocationFilter(final String packagePattern,
                                final String classPattern) {

        // Assign internal state
        setupInternalState(LocationScope.CLASS);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
        compilePattern(LocationScope.CLASS, classPattern);
    }

    public SourceLocationFilter(final String packagePattern,
                                final String classPattern,
                                final String methodPattern) {
        // Assign internal state
        setupInternalState(LocationScope.METHOD);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
        compilePattern(LocationScope.CLASS, classPattern);
        compilePattern(LocationScope.METHOD, methodPattern);
    }

    public SourceLocationFilter(final String packagePattern,
                                final String classPattern,
                                final String methodPattern,
                                final String lineNumberPattern) {

        // Assign internal state
        setupInternalState(LocationScope.LINE);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
        compilePattern(LocationScope.CLASS, classPattern);
        compilePattern(LocationScope.METHOD, methodPattern);
        compilePattern(LocationScope.LINE, lineNumberPattern);
    }

    public SourceLocationFilter(final String packagePattern,
                                final String classPattern,
                                final String methodPattern,
                                final String lineNumberPattern,
                                final String branchSegmentPattern) {

        // Assign internal state
        setupInternalState(LocationScope.SEGMENT);

        // Compile patterns and check sanity
        compilePattern(LocationScope.PACKAGE, packagePattern);
        compilePattern(LocationScope.CLASS, classPattern);
        compilePattern(LocationScope.METHOD, methodPattern);
        compilePattern(LocationScope.LINE, lineNumberPattern);
        compilePattern(LocationScope.SEGMENT, branchSegmentPattern);
    }

    /**
     * Retrieves the lowest LocationScope used to filter incoming SourceLocations.
     * For example, if this SourceLocationFilter has {@code LocationScope.CLASS }, then
     * package name and class name attributes are used to filter SourceLocations (but
     * method, lineNumber and segment attributes are not).
     *
     * @return The lowest scope used to filter SourceLocation instances, implying that Patterns for the given
     * scope and all above it are used to filter SourceLocations.
     */
    public final LocationScope getScope() {
        return scope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final SourceLocation candidate) {

        if(acceptAllInProject) {
            return true;
        } else {
            for(LocationScope current : LocationScope.values()) {

                if(current.compareTo(scope) > 0) {
                    break;
                }

                // Compare

            }
        }
        return false;
    }

    //
    // Private helpers
    //

    private void setupInternalState(final LocationScope scope) {
        locationPatternMap = new TreeMap<LocationScope, Pattern>();
        acceptAllInProject = scope == LocationScope.PROJECT;
        this.scope = scope;
    }

    private void compilePattern(final LocationScope location, final String javaRegExp) {

        // Check sanity
        Validate.notEmpty(javaRegExp, "Cannot handle null or empty " + location.toString().toLowerCase()
                + " scope java regexp.");

        // All done.
        locationPatternMap.put(location, Pattern.compile(javaRegExp));
    }
}
