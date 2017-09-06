/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.exceptions.CdbServiceException;



/**
 * @author aformic
 *
 */
public interface IFilteringCriteria {

	List<BooleanExpression> createFilteringConditions(List<SearchCriteria> criteria) throws CdbServiceException;
}
