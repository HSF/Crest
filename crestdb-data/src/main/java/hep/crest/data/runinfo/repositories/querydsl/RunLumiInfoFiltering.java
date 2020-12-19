/**
 *
 */
package hep.crest.data.runinfo.repositories.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.data.repositories.querydsl.SearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aformic
 *
 */
@Component("runFiltering")
public class RunLumiInfoFiltering implements IFilteringCriteria {

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
    public List<BooleanExpression> createFilteringConditions(List<SearchCriteria> criteria) {
        final List<BooleanExpression> expressions = new ArrayList<>();
        for (final SearchCriteria searchCriteria : criteria) {
            log.debug("search criteria {} {} {}", searchCriteria.getKey(),
                    searchCriteria.getOperation(), searchCriteria.getValue());
            if (searchCriteria.getKey().equals("run")) {
                final BooleanExpression runxthan = RunLumiInfoPredicates.isRunXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(runxthan);
            }
            else if (searchCriteria.getKey().equals("lb")) {
                final BooleanExpression lbxthan = RunLumiInfoPredicates.isLBXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(lbxthan);
            }
            else if (searchCriteria.getKey().equals("insertionTime")) {
                final BooleanExpression insertionTimexthan = RunLumiInfoPredicates
                        .isInsertionTimeXThan(searchCriteria.getOperation(),
                                searchCriteria.getValue().toString());
                expressions.add(insertionTimexthan);
            }
            else if (searchCriteria.getKey().equals("since")) {
                final BooleanExpression isSinceXThan = RunLumiInfoPredicates.isSinceXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(isSinceXThan);
            }
            else if (searchCriteria.getKey().equals("starttime")) {
                final BooleanExpression isStarttimeXThan = RunLumiInfoPredicates.isStarttimeXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(isStarttimeXThan);
            }
            else if (searchCriteria.getKey().equals("endtime")) {
                final BooleanExpression isEndtimeXThan = RunLumiInfoPredicates.isEndtimeXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(isEndtimeXThan);
            }
        }
        return expressions;
    }

}
