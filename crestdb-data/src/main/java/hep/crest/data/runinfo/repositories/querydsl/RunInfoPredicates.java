/**
 * 
 */
package hep.crest.data.runinfo.repositories.querydsl;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.runinfo.pojo.QRunInfo;

/**
 * Querydsl conditions.
 *
 * @author aformic
 *
 */
public final class RunInfoPredicates {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(RunInfoPredicates.class);

    /**
     * Default Ctor.
     */
    private RunInfoPredicates() {

    }

    /**
     * @param oper
     *            the String
     * @param num
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression isRunXThan(String oper, String num) {
        log.debug("isRunXThan: argument {}  operation {} ", num, oper);
        BooleanExpression pred = null;

        if (oper.equals("<")) {
            pred = QRunInfo.runInfo.runNumber.lt(new BigDecimal(num));
        }
        else if (oper.equals(">")) {
            pred = QRunInfo.runInfo.runNumber.gt(new BigDecimal(num));
        }
        else if (oper.equals(":")) {
            pred = QRunInfo.runInfo.runNumber.eq(new BigDecimal(num));
        }
        return pred;
    }

    /**
     * @param oper
     *            the String
     * @param num
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression isStartTimeXThan(String oper, String num) {
        log.debug("isStartTimeXThan: argument {}  operation {} ", num, oper);
        BooleanExpression pred = null;

        if (oper.equals("<")) {
            pred = QRunInfo.runInfo.startTime.lt(new Date(new Long(num)));
        }
        else if (oper.equals(">")) {
            pred = QRunInfo.runInfo.startTime.gt(new Date(new Long(num)));
        }
        else if (oper.equals(":")) {
            pred = QRunInfo.runInfo.startTime.eq(new Date(new Long(num)));
        }
        return pred;
    }

    /**
     * @param oper
     *            the String
     * @param num
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression isEndTimeXThan(String oper, String num) {
        log.debug("isEndTimeXThan: argument {}  operation {} ", num, oper);
        BooleanExpression pred = null;

        if (oper.equals("<")) {
            pred = QRunInfo.runInfo.endTime.lt(new Date(new Long(num)));
        }
        else if (oper.equals(">")) {
            pred = QRunInfo.runInfo.endTime.gt(new Date(new Long(num)));
        }
        else if (oper.equals(":")) {
            pred = QRunInfo.runInfo.endTime.eq(new Date(new Long(num)));
        }
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