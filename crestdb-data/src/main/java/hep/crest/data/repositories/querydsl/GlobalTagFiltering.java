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
@Component("globalTagFiltering")
public class GlobalTagFiltering implements IFilteringCriteria {

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
				if (searchCriteria.getKey().equals("workflow")) {
					BooleanExpression wflike = GlobalTagPredicates.hasWorkflowLike(searchCriteria.getValue().toString());
					expressions.add(wflike);
				} else if (searchCriteria.getKey().equals("name")) {
					BooleanExpression namelike = GlobalTagPredicates.hasNameLike(searchCriteria.getValue().toString());
					expressions.add(namelike);
				} else if (searchCriteria.getKey().equals("release")) {
					BooleanExpression releaselike = GlobalTagPredicates
							.hasReleaseLike(searchCriteria.getValue().toString());
					expressions.add(releaselike);
				} else if (searchCriteria.getKey().equals("scenario")) {
					BooleanExpression scenariolike = GlobalTagPredicates
							.hasScenarioLike(searchCriteria.getValue().toString());
					expressions.add(scenariolike);
				} else if (searchCriteria.getKey().equals("validity")) {
					BooleanExpression validityxthan = GlobalTagPredicates.isValidityXThan(searchCriteria.getOperation(),
							searchCriteria.getValue().toString());
					expressions.add(validityxthan);
				} else if (searchCriteria.getKey().equals("insertionTime")) {
					BooleanExpression insertionTimexthan = GlobalTagPredicates
							.isInsertionTimeXThan(searchCriteria.getOperation(), searchCriteria.getValue().toString());
					expressions.add(insertionTimexthan);
				} else if (searchCriteria.getKey().equals("type")) {
					BooleanExpression typeeq = GlobalTagPredicates.isType(searchCriteria.getValue().toString());
					expressions.add(typeeq);
				}
			}
			return expressions;
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}

}
