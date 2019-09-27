/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.pojo.QIov;


/**
 * @author aformic
 *
 */
public class IovPredicates {

	private static Logger log = LoggerFactory.getLogger(IovPredicates.class);

	private IovPredicates() {

	}

	/**
	 * @param tagname
	 * @return
	 */
	public static BooleanExpression hasTagName(String tagname) {
		log.debug("hasTagName: argument {}",tagname);
		return  QIov.iov.id.tagName.eq(tagname);
	}

	/**
	 * @param since
	 * @param until
	 * @return
	 */
	public static BooleanExpression hasSinceBetween(BigDecimal since, BigDecimal until) {
		log.debug("hasSinceBetween: argument {} {}",since,until);
		return  QIov.iov.id.since.between(since,until);
	}

	/**
	 * @param since
	 * @param until
	 * @return
	 */
	public static BooleanExpression isSinceXThan(String oper, BigDecimal since) {
		log.debug("isSinceXThan: argument {} {}",since,oper);
		BooleanExpression pred = null;

		if (oper.equals("<")) {
			pred = QIov.iov.id.since.lt(since);
		} else if (oper.equals(">")) {
			pred = QIov.iov.id.since.gt(since);
		} else if (oper.equals(":")) {
			pred = QIov.iov.id.since.eq(since);
		} 
		return pred;
	}

	
	public static BooleanExpression isInsertionTimeXThan(String oper, String num) {
		log.debug("isInsertionTimeXThan: argument {} operation {}",num ,oper);
		BooleanExpression pred = null;

		if (oper.equals("<")) {
			pred = QIov.iov.id.insertionTime.lt(new Date(new Long(num)));
		} else if (oper.equals(">")) {
			pred = QIov.iov.id.insertionTime.gt(new Date(new Long(num)));
		} else if (oper.equals(":")) {
			pred = QIov.iov.id.insertionTime.eq(new Date(new Long(num)));
		}
		return pred;
	}

	public static Predicate where(BooleanExpression exp) {
		return exp;
	}
}
