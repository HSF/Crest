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
 * @author aformic
 *
 */
@Component("tagFiltering")
public class TagFiltering implements IFilteringCriteria {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * hep.phycdb.svc.querydsl.IFilteringCriteria#createFilteringConditions(java
	 * .util.List, java.lang.Object)
	 */
	@Override
	public List<BooleanExpression> createFilteringConditions(List<SearchCriteria> criteria) throws CdbServiceException {
		try {
			List<BooleanExpression> expressions = new ArrayList<>();
			for (SearchCriteria searchCriteria : criteria) {
				log.debug("search criteria " + searchCriteria.getKey() + " " + searchCriteria.getOperation() + " "
						+ searchCriteria.getValue());
				if (searchCriteria.getKey().equals("objectType")) {
					BooleanExpression objtyplike = TagPredicates.hasObjectTypeLike(searchCriteria.getValue().toString());
					expressions.add(objtyplike);
				} else if (searchCriteria.getKey().equals("timeType")) {
					BooleanExpression timtyplike = TagPredicates.hasTimeTypeLike(searchCriteria.getValue().toString());
					expressions.add(timtyplike);
				} else if (searchCriteria.getKey().equals("insertionTime")) {
					BooleanExpression insertionTimexthan = TagPredicates
							.isInsertionTimeXThan(searchCriteria.getOperation(), searchCriteria.getValue().toString());
					expressions.add(insertionTimexthan);
				} else if (searchCriteria.getKey().equals("modificationTime")) {
					BooleanExpression modTimexthan = TagPredicates
							.isModificationTimeXThan(searchCriteria.getOperation(), searchCriteria.getValue().toString());
					expressions.add(modTimexthan);
				} else if (searchCriteria.getKey().equals("name")) {
					BooleanExpression namelike = TagPredicates
							.hasNameLike(searchCriteria.getValue().toString());
					expressions.add(namelike);
				} 
			}
			return expressions;
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}

}
