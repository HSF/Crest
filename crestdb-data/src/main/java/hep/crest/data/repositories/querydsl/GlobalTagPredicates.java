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

import hep.crest.data.pojo.QGlobalTag;

/**
 * @author aformic
 *
 */
public class GlobalTagPredicates {

	private static Logger log = LoggerFactory.getLogger(GlobalTagPredicates.class);

	private GlobalTagPredicates() {

	}

	public static BooleanExpression hasReleaseLike(String release) {
		log.debug("hasReleaseLike: argument {} ",release);
		return QGlobalTag.globalTag.release.like("%" + release + "%");
	}

	public static BooleanExpression hasWorkflowLike(String wf) {
		log.debug("hasWorkflowLike: argument {} ",wf);
		return QGlobalTag.globalTag.workflow.like("%" + wf + "%");
	}

	public static BooleanExpression hasNameLike(String name) {
		log.debug("hasNameLike: argument {} ",name);
		return QGlobalTag.globalTag.name.like("%" + name + "%");
	}
	
	public static BooleanExpression hasScenarioLike(String scenario) {
		log.debug("hasScenarioLike: argument {} ",scenario);
		return QGlobalTag.globalTag.scenario.like("%" + scenario + "%");
	}

	public static BooleanExpression isValidityXThan(String oper, String num) {
		log.debug("isValidity: argument {}  operation {}",num, oper);
		BooleanExpression pred = null;

		if (oper.equals("<")) {
			pred = QGlobalTag.globalTag.validity.lt(new BigDecimal(num));
		} else if (oper.equals(">")) {
			pred = QGlobalTag.globalTag.validity.gt(new BigDecimal(num));
		} else if (oper.equals(":")) {
			pred = QGlobalTag.globalTag.validity.eq(new BigDecimal(num));
		}
		return pred;
	}
	
	public static BooleanExpression isInsertionTimeXThan(String oper, String num) {
		log.debug("isInsertionTimeXThan: argument {} operation {}",num, oper);
		BooleanExpression pred = null;

		if (oper.equals("<")) {
			pred = QGlobalTag.globalTag.insertionTime.lt(new Date(new Long(num)));
		} else if (oper.equals(">")) {
			pred = QGlobalTag.globalTag.insertionTime.gt(new Date(new Long(num)));
		} else if (oper.equals(":")) {
			pred = QGlobalTag.globalTag.insertionTime.eq(new Date(new Long(num)));
		}
		return pred;
	}

	public static BooleanExpression isType(String typestr) {
		log.debug("isType: argument {} ",typestr );
		Character type = typestr.charAt(0);
		BooleanExpression pred = null;
		pred = QGlobalTag.globalTag.type.eq(type);
		return pred;
	}

	public static Predicate where(BooleanExpression exp) {
		return exp;
	}
}
