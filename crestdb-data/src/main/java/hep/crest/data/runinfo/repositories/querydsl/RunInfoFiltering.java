/**
 * 
 */
package hep.crest.data.runinfo.repositories.querydsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;

/**
 * The utility filtering class to handle SQL requests for folder selection. The
 * methods used are implemented in @see RunInfoPredicates.
 *
 * @author aformic
 *
 */
@Component("runFiltering")
public class RunInfoFiltering implements IFilteringCriteria {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RunInfoFiltering.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * hep.phycdb.svc.querydsl.IFilteringCriteria#createFilteringConditions(java
     * .util.List, java.lang.Object)
     */
    @Override
    public List<BooleanExpression> createFilteringConditions(List<SearchCriteria> criteria)
            throws CdbServiceException {
        final List<BooleanExpression> expressions = new ArrayList<>();
        for (final SearchCriteria searchCriteria : criteria) {
            log.debug("search criteria {} {} {}", searchCriteria.getKey(),
                    searchCriteria.getOperation(), searchCriteria.getValue());
            final String key = searchCriteria.getKey().toLowerCase(Locale.ENGLISH);
            if ("runnumber".equals(key)) {
                // Filter based on the runnumber.
                final BooleanExpression runxthan = RunInfoPredicates.isRunXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(runxthan);
            }
            else if ("starttime".equals(key)) {
                // Filter based on the start time.
                final BooleanExpression startTimexthan = RunInfoPredicates.isStartTimeXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(startTimexthan);
            }
            else if ("endtime".equals(key)) {
                // Filter based on the end time.
                final BooleanExpression endTimexthan = RunInfoPredicates.isEndTimeXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(endTimexthan);
            }
        }
        return expressions;
    }

}
