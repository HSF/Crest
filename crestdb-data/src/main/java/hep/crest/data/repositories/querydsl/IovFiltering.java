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
@Component("iovFiltering")
public class IovFiltering implements IFilteringCriteria {

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
				if (searchCriteria.getKey().equals("tagname")) {
					BooleanExpression objtyplike = IovPredicates.hasTagName(searchCriteria.getValue().toString());
					expressions.add(objtyplike);
				} 
				if (searchCriteria.getKey().equals("tagid")) {
					Long tagid = new Long(searchCriteria.getValue().toString());
					BooleanExpression objtypeq = IovPredicates.hasTagId(tagid);
					expressions.add(objtypeq);
				} 
			}
			return expressions;
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}

}
