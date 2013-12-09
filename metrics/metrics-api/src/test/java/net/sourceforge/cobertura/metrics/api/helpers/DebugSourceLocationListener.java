package net.sourceforge.cobertura.metrics.api.helpers;

import net.sourceforge.cobertura.metrics.api.event.AbstractSourceLocationListener;
import net.sourceforge.cobertura.metrics.model.location.SourceLocation;

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class DebugSourceLocationListener extends AbstractSourceLocationListener {

    public ConcurrentNavigableMap<SourceLocation, Integer> recordedSteps
            = new ConcurrentSkipListMap<SourceLocation, Integer>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addInternalExecutionSteps(final SourceLocation nonNullLocation, final int positiveNumSteps) {

        Integer numSteps = recordedSteps.get(nonNullLocation);
        if(numSteps == null) {
            recordedSteps.put(nonNullLocation, positiveNumSteps);
        } else {
            recordedSteps.put(nonNullLocation, numSteps + positiveNumSteps);
        }
    }
}
