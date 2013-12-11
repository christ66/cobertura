package net.sourceforge.cobertura.metrics.model.coverage;

import net.sourceforge.cobertura.metrics.model.LocationScope;

import java.util.EnumMap;
import java.util.regex.Pattern;

/**
 * CoverageScope implementation for Projects, sporting only a project name.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProjectCoverageScope extends AbstractCoverageScope {

    /**
     * JAXB-friendly constructor.
     */
    public ProjectCoverageScope() {
        super();
    }

    /**
     * Compound constructor invoked by subclasses.
     * The constructor will, in turn, call the template setup method {@code setupPatternMap} to configure
     * the patternMap.
     *
     * @param projectName The human-readable project name.
     * @throws IllegalStateException if the patternMap, populated from the {@code setupPatternMap} method
     *                               held null patterns.
     */
    public ProjectCoverageScope(final String projectName) throws IllegalStateException {
        super(projectName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupPatternMap(final EnumMap<LocationScope, Pattern> patternMap) {
        // Do nothing; the default setup for the patternMap works well in this case
    }
}
