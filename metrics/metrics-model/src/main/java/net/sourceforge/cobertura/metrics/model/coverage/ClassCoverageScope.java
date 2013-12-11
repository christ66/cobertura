package net.sourceforge.cobertura.metrics.model.coverage;

import net.sourceforge.cobertura.metrics.model.LocationScope;

import java.util.EnumMap;
import java.util.regex.Pattern;

/**
 * CoverageScope implementation for a single Class (without any inner classes included).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ClassCoverageScope extends AbstractCoverageScope {

    // Internal state
    private transient Class<?> theClass;

    /**
     * JAXB-friendly constructor.
     */
    public ClassCoverageScope() {
    }

    /**
     * Compound constructor invoked by subclasses.
     * The constructor will, in turn, call the template setup method {@code setupPatternMap} to configure
     * the patternMap.
     *
     * @param aClass The class for which this ClassCoverageScope should be generated.
     * @throws IllegalStateException if the patternMap, populated from the {@code setupPatternMap} method
     *                                         held null patterns.
     */
    public ClassCoverageScope(final Class<?> aClass) throws IllegalStateException {
        super(aClass.getName());

        // Assign internal state
        theClass = aClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupPatternMap(EnumMap<LocationScope, Pattern> patternMap) {

        final String packagePattern = theClass.getPackage().getName().replaceAll("\\.", "\\\\.");
        final String classPattern = theClass.getSimpleName();

        patternMap.put(LocationScope.PACKAGE, Pattern.compile(packagePattern));
        patternMap.put(LocationScope.CLASS, Pattern.compile(classPattern));
    }
}
