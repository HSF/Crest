/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * @author aformic
 *
 */
public interface IFilteringCriteria {

    /**
     * @param criteria
     *            the List<SearchCriteria>
     * @return List<BooleanExpression>
     */
    List<BooleanExpression> createFilteringConditions(List<SearchCriteria> criteria);
}
