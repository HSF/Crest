/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.security.pojo.QCrestFolders;

/**
 * @author aformic
 *
 */
public final class FolderPredicates {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(FolderPredicates.class);

    /**
     * Default ctor.
     */
    private FolderPredicates() {

    }

    /**
     * @param nfp
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression hasNodeFullpathLike(String nfp) {
        log.debug("hasNodeFullpathLike: argument {}", nfp);
        return QCrestFolders.crestFolders.nodeFullpath.like("%" + nfp + "%");
    }

    /**
     * @param tagpt
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression hasTagPatternLike(String tagpt) {
        log.debug("hasTagPatternLike: argument {}", tagpt);
        return QCrestFolders.crestFolders.tagPattern.like("%" + tagpt + "%");
    }

    /**
     * @param gr
     *            the String
     * @return BooleanExpression
     */
    public static BooleanExpression hasGroupRoleLike(String gr) {
        log.debug("hasGroupRoleLike: argument {}", gr);
        return QCrestFolders.crestFolders.groupRole.like("%" + gr + "%");
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
