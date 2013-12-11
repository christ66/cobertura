package net.sourceforge.cobertura.metrics.api.helpers;

import net.sourceforge.cobertura.metrics.api.AbstractCoverageCalculator;
import net.sourceforge.cobertura.metrics.api.CoverageType;
import net.sourceforge.cobertura.metrics.api.event.AbstractSourceLocationListener;
import net.sourceforge.cobertura.metrics.api.event.SourceLocationListener;
import net.sourceforge.cobertura.metrics.api.location.SourceLocationFilter;
import net.sourceforge.cobertura.metrics.model.coverage.CoverageRecord;
import net.sourceforge.cobertura.metrics.model.coverage.Rate;
import net.sourceforge.cobertura.metrics.model.location.SourceLocation;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class DebugCoverageCalculator extends AbstractCoverageCalculator {

    // Shared state
    public final SortedMap<SourceLocation, CoverageRecord> coverageRecords
            = new TreeMap<SourceLocation, CoverageRecord>();
    public SourceLocationListener listener = new AbstractSourceLocationListener() {

        private final Object lock = new Object();

        /**
         * {@inheritDoc}
         */
        @Override
        protected void addInternalExecutionSteps(final SourceLocation nonNullLocation,
                                                 final int positiveNumSteps) {

            synchronized (lock) {
                CoverageRecord coverageRecord = coverageRecords.get(nonNullLocation);

                if (coverageRecord == null) {
                    coverageRecord = new CoverageRecord(nonNullLocation, positiveNumSteps);
                } else {
                    coverageRecord.addHits(positiveNumSteps);
                }

                // Write the record back into the Map.
                coverageRecords.put(nonNullLocation, coverageRecord);
            }
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rate getRate(final SourceLocationFilter nonNullFilter, final CoverageType nonNullCoverageType) {

        if(nonNullCoverageType == CoverageType.BRANCH) {
            throw new IllegalArgumentException("Cannot handle branch coverage.");
        }

        final Filter<Tuple<SourceLocation, CoverageRecord>> filter = new Filter<Tuple<SourceLocation, CoverageRecord>>() {
            @Override
            public boolean accept(final Tuple<SourceLocation, CoverageRecord> candidate) {
                return candidate != null && nonNullFilter.accept(candidate.getKey());
            }
        };

        final Map<SourceLocation, CoverageRecord> result = CollectionAlgorithms.filter(coverageRecords, filter);
        int hits = 0;
        for(CoverageRecord current : result.values()) {
            if(current.getHitCount() != 0) {
                hits++;
            }
        }

        // All done.
        return new Rate(result.size(), hits, CoverageType.LINE.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceLocationListener getSourceLocationListener() {
        return listener;
    }
}
