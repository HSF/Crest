/**
 * 
 */
package hep.crest.data.runinfo.repositories.querydsl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;

/**
 * @author aformic
 *
 */
@Component("runFiltering")
public class RunInfoFiltering implements IFilteringCriteria {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
        try {
            final List<BooleanExpression> expressions = new ArrayList<>();
            for (final SearchCriteria searchCriteria : criteria) {
                log.debug("search criteria " + searchCriteria.getKey() + " "
                        + searchCriteria.getOperation() + " " + searchCriteria.getValue());
                final String key = searchCriteria.getKey().toLowerCase();
                if (key.equals("runnumber")) {
                    final BooleanExpression runxthan = RunInfoPredicates.isRunXThan(
                            searchCriteria.getOperation(), searchCriteria.getValue().toString());
                    expressions.add(runxthan);
                }
                else if (key.equals("starttime")) {
                    final BooleanExpression startTimexthan = RunInfoPredicates
                            .isStartTimeXThan(searchCriteria.getOperation(),
                                    searchCriteria.getValue().toString());
                    expressions.add(startTimexthan);
                }
                else if (key.equals("endtime")) {
                    final BooleanExpression endTimexthan = RunInfoPredicates
                            .isEndTimeXThan(searchCriteria.getOperation(),
                                    searchCriteria.getValue().toString());
                    expressions.add(endTimexthan);
                }
            }
            return expressions;
        }
        catch (final Exception e) {
            throw new CdbServiceException(e.getMessage());
        }
    }

}
