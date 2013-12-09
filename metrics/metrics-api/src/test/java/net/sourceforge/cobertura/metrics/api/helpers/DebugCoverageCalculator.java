package net.sourceforge.cobertura.metrics.api.helpers;

import net.sourceforge.cobertura.metrics.api.AbstractCoverageCalculator;
import net.sourceforge.cobertura.metrics.api.CoverageType;
import net.sourceforge.cobertura.metrics.api.event.SourceLocationListener;
import net.sourceforge.cobertura.metrics.model.coverage.Rate;
import net.sourceforge.cobertura.metrics.model.location.SourceLocation;

import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class DebugCoverageCalculator extends AbstractCoverageCalculator {

    public DebugSourceLocationListener listener = new DebugSourceLocationListener();

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rate getRate(final CoverageType nonNullCoverageType) {

        Rate toReturn = new Rate();

        for(Map.Entry<SourceLocation, Integer> current : listener.recordedSteps.entrySet()) {

        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceLocationListener getSourceLocationListener() {
        return listener;
    }
}
