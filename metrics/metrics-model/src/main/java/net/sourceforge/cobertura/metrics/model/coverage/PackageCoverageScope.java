package net.sourceforge.cobertura.metrics.model.coverage;

import net.sourceforge.cobertura.metrics.model.LocationScope;

import java.util.EnumMap;
import java.util.regex.Pattern;

/**
 * CoverageScope implementation for a single Package (without any subpackages included).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PackageCoverageScope extends AbstractCoverageScope {

    /**
     * JAXB-friendly constructor.
     */
    public PackageCoverageScope() {
    }

    /**
     * Compound constructor, creating a CoverageScope for a singular Package
     * (i.e. not including subpackages).
     *
     * @param scopePackage The package which should constitute this PackageCoverageScope.
     * @throws IllegalStateException if the patternMap, populated from the {@code setupPatternMap} method
     *                                         held null patterns.
     */
    public PackageCoverageScope(final Package scopePackage) throws IllegalStateException {
        super(scopePackage.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupPatternMap(final EnumMap<LocationScope, Pattern> patternMap) {
        patternMap.put(LocationScope.PACKAGE, Pattern.compile(getName().replaceAll("\\.", "\\\\.")));
    }
}
