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

import hep.crest.data.runinfo.pojo.QRunLumiInfo;

/**
 * Querydsl conditions.
 * 
 * @author aformic
 *
 */
public class RunLumiInfoPredicates {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(RunLumiInfoPredicates.class);

    /**
     * Default Ctor.
     */
    private RunLumiInfoPredicates() {

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
            pred = QRunLumiInfo.runLumiInfo.run.lt(new BigDecimal(num));
        }
        else if (oper.equals(">")) {
            pred = QRunLumiInfo.runLumiInfo.run.gt(new BigDecimal(num));
        }
        else if (oper.equals(":")) {
            pred = QRunLumiInfo.runLumiInfo.run.eq(new BigDecimal(num));
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
    public static BooleanExpression isLBXThan(String oper, String num) {
        log.debug("isLBXThan: argument {}  operation {} ", num, oper);
        BooleanExpression pred = null;

        if (oper.equals("<")) {
            pred = QRunLumiInfo.runLumiInfo.lb.lt(new BigDecimal(num));
        }
        else if (oper.equals(">")) {
            pred = QRunLumiInfo.runLumiInfo.lb.gt(new BigDecimal(num));
        }
        else if (oper.equals(":")) {
            pred = QRunLumiInfo.runLumiInfo.lb.eq(new BigDecimal(num));
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
    public static BooleanExpression isSinceXThan(String oper, String num) {
        log.debug("isSinceXThan: argument {}  operation {} ", num, oper);
        BooleanExpression pred = null;

        if (oper.equals("<")) {
            pred = QRunLumiInfo.runLumiInfo.since.lt(new BigDecimal(num));
        }
        else if (oper.equals(">")) {
            pred = QRunLumiInfo.runLumiInfo.since.gt(new BigDecimal(num));
        }
        else if (oper.equals(":")) {
            pred = QRunLumiInfo.runLumiInfo.since.eq(new BigDecimal(num));
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
    public static BooleanExpression isStarttimeXThan(String oper, String num) {
        log.debug("isStarttimeXThan: argument {}  operation {} ", num, oper);
        BooleanExpression pred = null;

        if (oper.equals("<")) {
            pred = QRunLumiInfo.runLumiInfo.starttime.lt(new BigDecimal(num));
        }
        else if (oper.equals(">")) {
            pred = QRunLumiInfo.runLumiInfo.starttime.gt(new BigDecimal(num));
        }
        else if (oper.equals(":")) {
            pred = QRunLumiInfo.runLumiInfo.starttime.eq(new BigDecimal(num));
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
    public static BooleanExpression isEndtimeXThan(String oper, String num) {
        log.debug("isEndtimeXThan: argument {}  operation {} ", num, oper);
        BooleanExpression pred = null;

        if (oper.equals("<")) {
            pred = QRunLumiInfo.runLumiInfo.endtime.lt(new BigDecimal(num));
        }
        else if (oper.equals(">")) {
            pred = QRunLumiInfo.runLumiInfo.endtime.gt(new BigDecimal(num));
        }
        else if (oper.equals(":")) {
            pred = QRunLumiInfo.runLumiInfo.endtime.eq(new BigDecimal(num));
        }
        return pred;
    }

    /**
     * @param since
     *            the BigDecimal
     * @param until
     *            the BigDecimal
     * @return BooleanExpression
     */
    public static BooleanExpression hasSinceBetween(BigDecimal since, BigDecimal until) {
        log.debug("hasSinceBetween: argument {} {} ", since, until);
        return QRunLumiInfo.runLumiInfo.since.between(since, until);
    }

    /**
     * @param since
     *            the BigDecimal
     * @param until
     *            the BigDecimal
     * @return BooleanExpression
     */
    public static BooleanExpression hasStarttimeBetween(BigDecimal since, BigDecimal until) {
        log.debug("hasStarttimeBetween: argument {} {} ", since, until);
        return QRunLumiInfo.runLumiInfo.starttime.between(since, until);
    }

    /**
     * @param since
     *            the BigDecimal
     * @param until
     *            the BigDecimal
     * @return BooleanExpression
     */
    public static BooleanExpression hasEndtimeBetween(BigDecimal since, BigDecimal until) {
        log.debug("hasEndtimeBetween: argument {} {} ", since, until);
        return QRunLumiInfo.runLumiInfo.endtime.between(since, until);
    }

    /**
     * @param oper
     *            the String
     * @param num
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression isInsertionTimeXThan(String oper, String num) {
        log.debug("isInsertionTimeXThan: argument {}  operation {} ", num, oper);
        BooleanExpression pred = null;

        if (oper.equals("<")) {
            pred = QRunLumiInfo.runLumiInfo.insertionTime.lt(new Date(new Long(num)));
        }
        else if (oper.equals(">")) {
            pred = QRunLumiInfo.runLumiInfo.insertionTime.gt(new Date(new Long(num)));
        }
        else if (oper.equals(":")) {
            pred = QRunLumiInfo.runLumiInfo.insertionTime.eq(new Date(new Long(num)));
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
