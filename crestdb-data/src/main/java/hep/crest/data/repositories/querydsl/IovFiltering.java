/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import java.math.BigDecimal;
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
 * methods used are implemented in @see IovPredicates.
 *
 * @author aformic
 *
 */
@Component("iovFiltering")
public class IovFiltering implements IFilteringCriteria {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IovFiltering.class);

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
            if ("tagname".equals(key)) {
                // Filter based on the tag name.
                final BooleanExpression objtyplike = IovPredicates
                        .hasTagName(searchCriteria.getValue().toString());
                expressions.add(objtyplike);
            }
            else if ("insertiontime".equals(key)) {
                // Filter based on the insertion time.
                final BooleanExpression insertionTimexthan = IovPredicates.isInsertionTimeXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(insertionTimexthan);
            }
            else if ("since".equals(key)) {
                // Filter based on the since time.
                final BigDecimal since = new BigDecimal(searchCriteria.getValue().toString());
                final BooleanExpression sincexthan = IovPredicates
                        .isSinceXThan(searchCriteria.getOperation(), since);
                expressions.add(sincexthan);
            }
        }
        return expressions;
    }

}
