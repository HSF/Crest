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
 * The utility filtering class to handle SQL requests for folder selection.
 * The methods used are implemented in @see FolderPredicates.
 * @author aformic
 *
 */
@Component("folderFiltering")
public class FolderFiltering implements IFilteringCriteria {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FolderFiltering.class);

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
            // Set to lower case for comparison
            final String key = searchCriteria.getKey().toLowerCase(Locale.ENGLISH);
            if ("nodefullpath".equals(key)) {
                // Filter based on nodefullpath
                final BooleanExpression namelike = FolderPredicates
                        .hasNodeFullpathLike(searchCriteria.getValue().toString());
                expressions.add(namelike);
            }
            else if ("tagpattern".equals(key)) {
                // Filter based on tagpattern
                final BooleanExpression namelike = FolderPredicates
                        .hasTagPatternLike(searchCriteria.getValue().toString());
                expressions.add(namelike);
            }
            else if ("grouprole".equals(key)) {
                // Filter based on grouprole
                final BooleanExpression namelike = FolderPredicates
                        .hasGroupRoleLike(searchCriteria.getValue().toString());
                expressions.add(namelike);
            }
        }
        return expressions;
    }
}
