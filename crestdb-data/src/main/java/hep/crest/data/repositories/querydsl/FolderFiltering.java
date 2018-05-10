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
@Component("folderFiltering")
public class FolderFiltering implements IFilteringCriteria {

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
				if (searchCriteria.getKey().equals("nodeFullpath")) {
					BooleanExpression namelike = FolderPredicates.hasNodeFullpathLike(searchCriteria.getValue().toString());
					expressions.add(namelike);
				} else if (searchCriteria.getKey().equals("tagPattern")) {
					BooleanExpression namelike = FolderPredicates.hasTagPatternLike(searchCriteria.getValue().toString());
					expressions.add(namelike);
				} else if (searchCriteria.getKey().equals("groupRole")) {
					BooleanExpression namelike = FolderPredicates.hasGroupRoleLike(searchCriteria.getValue().toString());
					expressions.add(namelike);
				}
			}
			return expressions;
		} catch (Exception e) {
			throw new CdbServiceException(e.getMessage());
		}
	}

}
