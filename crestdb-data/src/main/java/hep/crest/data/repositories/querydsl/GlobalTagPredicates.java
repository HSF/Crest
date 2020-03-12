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
public final class GlobalTagPredicates {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalTagPredicates.class);

    /**
     * Private ctor.
     */
    private GlobalTagPredicates() {

    }

    /**
     * @param release
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression hasReleaseLike(String release) {
        log.debug("hasReleaseLike: argument {} ", release);
        return QGlobalTag.globalTag.release.like("%" + release + "%");
    }

    /**
     * @param wf
     *            the String workflow
     * @return BooleanExpression
     */
    public static BooleanExpression hasWorkflowLike(String wf) {
        log.debug("hasWorkflowLike: argument {} ", wf);
        return QGlobalTag.globalTag.workflow.like("%" + wf + "%");
    }

    /**
     * @param name
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression hasNameLike(String name) {
        log.debug("hasNameLike: argument {} ", name);
        return QGlobalTag.globalTag.name.like("%" + name + "%");
    }

    /**
     * @param scenario
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression hasScenarioLike(String scenario) {
        log.debug("hasScenarioLike: argument {} ", scenario);
        return QGlobalTag.globalTag.scenario.like("%" + scenario + "%");
    }

    /**
     * @param oper
     *            the String operation
     * @param num
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression isValidityXThan(String oper, String num) {
        log.debug("isValidity: argument {}  operation {}", num, oper);
        BooleanExpression pred = null;

        if ("<".equals(oper)) {
            pred = QGlobalTag.globalTag.validity.lt(new BigDecimal(num));
        }
        else if (">".equals(oper)) {
            pred = QGlobalTag.globalTag.validity.gt(new BigDecimal(num));
        }
        else if (":".equals(oper)) {
            pred = QGlobalTag.globalTag.validity.eq(new BigDecimal(num));
        }
        return pred;
    }

    /**
     * @param oper
     *            the String operation
     * @param num
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression isInsertionTimeXThan(String oper, String num) {
        log.debug("isInsertionTimeXThan: argument {} operation {}", num, oper);
        BooleanExpression pred = null;

        if (oper.equals("<")) {
            pred = QGlobalTag.globalTag.insertionTime.lt(new Date(new Long(num)));
        }
        else if (oper.equals(">")) {
            pred = QGlobalTag.globalTag.insertionTime.gt(new Date(new Long(num)));
        }
        else if (oper.equals(":")) {
            pred = QGlobalTag.globalTag.insertionTime.eq(new Date(new Long(num)));
        }
        return pred;
    }

    /**
     * @param typestr
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression isType(String typestr) {
        log.debug("isType: argument {} ", typestr);
        final Character type = typestr.charAt(0);
        BooleanExpression pred = null;
        pred = QGlobalTag.globalTag.type.eq(type);
        return pred;
    }

    /**
     * @param exp
     *            the BooleanExpression
     * @return Predicate
     */
    public static Predicate where(BooleanExpression exp) {
        return exp;
    }
}
