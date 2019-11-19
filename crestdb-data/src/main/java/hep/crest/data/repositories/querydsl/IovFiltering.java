/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.exceptions.CdbServiceException;

/**
 * @author aformic
 *
 */
@Component("iovFiltering")
public class IovFiltering implements IFilteringCriteria {

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
                if (key.equals("tagname")) {
                    final BooleanExpression objtyplike = IovPredicates
                            .hasTagName(searchCriteria.getValue().toString());
                    expressions.add(objtyplike);
                }
                else if (key.equals("insertiontime")) {
                    final BooleanExpression insertionTimexthan = IovPredicates.isInsertionTimeXThan(
                            searchCriteria.getOperation(), searchCriteria.getValue().toString());
                    expressions.add(insertionTimexthan);
                }
                else if (key.equals("since")) {
                    final BigDecimal since = new BigDecimal(searchCriteria.getValue().toString());
                    final BooleanExpression sincexthan = IovPredicates
                            .isSinceXThan(searchCriteria.getOperation(), since);
                    expressions.add(sincexthan);
                }
            }
            return expressions;
        }
        catch (final Exception e) {
            throw new CdbServiceException(e.getMessage());
        }
    }
}
