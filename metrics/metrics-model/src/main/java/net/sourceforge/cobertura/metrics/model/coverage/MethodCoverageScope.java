package net.sourceforge.cobertura.metrics.model.coverage;

import net.sourceforge.cobertura.metrics.model.LocationScope;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.regex.Pattern;

/**
 * CoverageScope implementation for a single Method.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MethodCoverageScope extends AbstractCoverageScope {

    // Internal state
    private transient Method theMethod;

    /**
     * JAXB-friendly constructor.
     */
    public MethodCoverageScope() {
    }

    /**
     * Compound constructor invoked by subclasses.
     * The constructor will, in turn, call the template setup method {@code setupPatternMap} to configure
     * the patternMap.
     *
     * @param method The method which constitutes the CoverageScope.
     * @throws IllegalStateException if the patternMap, populated from the {@code setupPatternMap} method
     *                               held null patterns.
     */
    public MethodCoverageScope(final Method method) throws IllegalStateException {
        super(method.toGenericString());

        // Assign internal state
        theMethod = method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupPatternMap(EnumMap<LocationScope, Pattern> patternMap) {

        final Class<?> theClass = theMethod.getDeclaringClass();
        final String packagePattern = theClass.getPackage().getName().replaceAll("\\.", "\\\\.");
        final String classPattern = theClass.getSimpleName();

        patternMap.put(LocationScope.PACKAGE, Pattern.compile(packagePattern));
        patternMap.put(LocationScope.CLASS, Pattern.compile(classPattern));

        // TODO: Update to handle overloaded methods
        patternMap.put(LocationScope.METHOD, Pattern.compile(theMethod.getName()));
    }
}
