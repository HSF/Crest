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
public class FolderPredicates {

	private static Logger log = LoggerFactory.getLogger(FolderPredicates.class);

	private FolderPredicates() {

	}

	/**
	 * @param nfp
	 * @return
	 */
	public static BooleanExpression hasNodeFullpathLike(String nfp) {
		log.debug("hasNodeFullpathLike: argument {}",nfp);
		return QCrestFolders.crestFolders.nodeFullpath.like("%" + nfp + "%");
	}
	/**
	 * @param tagpt
	 * @return
	 */
	public static BooleanExpression hasTagPatternLike(String tagpt) {
		log.debug("hasTagPatternLike: argument {}",tagpt);
		return QCrestFolders.crestFolders.tagPattern.like("%" + tagpt + "%");
	}
	
	/**
	 * @param gr
	 * @return
	 */
	public static BooleanExpression hasGroupRoleLike(String gr) {
		log.debug("hasGroupRoleLike: argument {}",gr);
		return QCrestFolders.crestFolders.groupRole.like("%" + gr + "%");
	}

	/**
	 * @param exp
	 * @return
	 */
	public static Predicate where(BooleanExpression exp) {
		return exp;
	}
}
