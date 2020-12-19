/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.pojo.QIov;

/**
 * @author aformic
 *
 */
public final class IovPredicates {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IovPredicates.class);

    /**
     * Default ctor.
     */
    private IovPredicates() {

    }

    /**
     * @param tagname
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression hasTagName(String tagname) {
        log.debug("hasTagName: argument {}", tagname);
        return QIov.iov.id.tagName.eq(tagname);
    }

    /**
     * @param oper
     *            the String
     * @param since
     *            the BigDecimal
     * @return BooleanExpression
     */
    public static BooleanExpression isSinceXThan(String oper, BigDecimal since) {
        log.debug("isSinceXThan: argument {} {}", since, oper);
        BooleanExpression pred = null;

        if ("<".equals(oper)) {
            pred = QIov.iov.id.since.lt(since);
        }
        else if (">".equals(oper)) {
            pred = QIov.iov.id.since.gt(since);
        }
        else if (":".equals(oper)) {
            pred = QIov.iov.id.since.eq(since);
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
    public static BooleanExpression isInsertionTimeXThan(String oper, String num) {
        log.debug("isInsertionTimeXThan: argument {} operation {}", num, oper);
        BooleanExpression pred = null;

        if ("<".equals(oper)) {
            pred = QIov.iov.id.insertionTime.lt(new Date(Long.valueOf(num)));
        }
        else if (">".equals(oper)) {
            pred = QIov.iov.id.insertionTime.gt(new Date(Long.valueOf(num)));
        }
        else if (":".equals(oper)) {
            pred = QIov.iov.id.insertionTime.eq(new Date(Long.valueOf(num)));
        }
        return pred;
    }

}
