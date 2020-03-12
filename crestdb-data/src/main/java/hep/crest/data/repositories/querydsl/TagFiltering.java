/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.exceptions.CdbServiceException;

/**
 * The utility filtering class to handle SQL requests for folder selection. The
 * methods used are implemented in @see TagPredicates.
 *
 * @author aformic
 *
 */
@Component("tagFiltering")
public class TagFiltering implements IFilteringCriteria {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TagFiltering.class);

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
            final String key = searchCriteria.getKey().toLowerCase();
            if ("objecttype".equals(key) || "payloadspec".equals(key)) {
                // Filter based on object type or payload spec (this depends on the versions:
                // ATLAS or CMS).
                final BooleanExpression objtyplike = TagPredicates
                        .hasObjectTypeLike(searchCriteria.getValue().toString());
                expressions.add(objtyplike);
            }
            else if ("timetype".equals(key)) {
                // Filter based on time type.
                final BooleanExpression timtyplike = TagPredicates
                        .hasTimeTypeLike(searchCriteria.getValue().toString());
                expressions.add(timtyplike);
            }
            else if ("insertiontime".equals(key)) {
                // Filter based on insertion time.
                final BooleanExpression insertionTimexthan = TagPredicates.isInsertionTimeXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(insertionTimexthan);
            }
            else if ("modificationtime".equals(key)) {
                // Filter based on modification time.
                final BooleanExpression modTimexthan = TagPredicates.isModificationTimeXThan(
                        searchCriteria.getOperation(), searchCriteria.getValue().toString());
                expressions.add(modTimexthan);
            }
            else if ("name".equals(key)) {
                // Filter based on tag name.
                final BooleanExpression namelike = TagPredicates
                        .hasNameLike(searchCriteria.getValue().toString());
                expressions.add(namelike);
            }
        }
        return expressions;
    }
}
