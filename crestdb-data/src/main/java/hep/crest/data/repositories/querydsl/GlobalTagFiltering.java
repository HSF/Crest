/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.exceptions.CdbServiceException;

/**
 * The utility filtering class to handle SQL requests for folder selection. The
 * methods used are implemented in @see GlobalTagPredicates.
 *
 * @author aformic
 *
 */
@Component("globalTagFiltering")
public class GlobalTagFiltering implements IFilteringCriteria {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalTagFiltering.class);

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
        // Build the list of boolean expressions.
        for (final SearchCriteria searchCriteria : criteria) {
            log.debug("search criteria {} {} {}", searchCriteria.getKey(),
                    searchCriteria.getOperation(), searchCriteria.getValue());
            final String key = searchCriteria.getKey().toLowerCase(Locale.ENGLISH);
            if ("workflow".equals(key)) {
                // Filter based on the worlflow.
                final BooleanExpression wflike = GlobalTagPredicates
                        .hasWorkflowLike(searchCriteria.getValue().toString());
                expressions.add(wflike);
            }
            else if ("name".equals(key)) {
                // Filter based on the global tag name.
                final BooleanExpression namelike = GlobalTagPredicates
                        .hasNameLike(searchCriteria.getValue().toString());
                expressions.add(namelike);
            }
            else if ("release".equals(key)) {
                // Filter based on the release.
                final BooleanExpression releaselike = GlobalTagPredicates
                        .hasReleaseLike(searchCriteria.getValue().toString());
                expressions.add(releaselike);
            }
            else if ("scenario".equals(key)) {
                // Filter based on the scenario.
                final BooleanExpression scenariolike = GlobalTagPredicates
                        .hasScenarioLike(searchCriteria.getValue().toString());
                expressions.add(scenariolike);
            }
            else if ("validity".equals(key)) {
                // Filter based on the validity.
                final BooleanExpression validityxthan = GlobalTagPredicates.isValidityXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(validityxthan);
            }
            else if ("insertiontime".equals(key)) {
                // Filter based on the insertion time.
                final BooleanExpression insertionTimexthan = GlobalTagPredicates
                        .isInsertionTimeXThan(searchCriteria.getOperation(),
                                searchCriteria.getValue().toString());
                expressions.add(insertionTimexthan);
            }
            else if ("type".equals(key)) {
                // Filter based on the type of the global tag.
                final BooleanExpression typeeq = GlobalTagPredicates
                        .isType(searchCriteria.getValue().toString());
                expressions.add(typeeq);
            }
        }
        return expressions;
    }
}
