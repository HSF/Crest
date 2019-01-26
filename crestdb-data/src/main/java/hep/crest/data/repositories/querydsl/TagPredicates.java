/**
 * 
 */
package hep.crest.data.repositories.querydsl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import hep.crest.data.pojo.QTag;


/**
 * @author aformic
 *
 */
public class TagPredicates {

	private static Logger log = LoggerFactory.getLogger(TagPredicates.class);

	private TagPredicates() {

	}

	/**
	 * @param name
	 * @return
	 */
	public static BooleanExpression hasNameLike(String name) {
		log.debug("hasNameLike: argument {}",name);
		return QTag.tag.name.like("%" + name + "%");
	}
	
	/**
	 * @param ttype
	 * @return
	 */
	public static BooleanExpression hasTimeTypeLike(String ttype) {
		log.debug("hasTimeTypeLike: argument {}",ttype);
		return QTag.tag.timeType.like("%" + ttype + "%");
	}

	/**
	 * @param objtype
	 * @return
	 */
	public static BooleanExpression hasObjectTypeLike(String objtype) {
		log.debug("hasObjectTypeLike: argument {}",objtype);
		return QTag.tag.objectType.like("%"+objtype+"%");
	}
	
	/**
	 * @param oper
	 * @param num
	 * @return
	 */
	public static BooleanExpression isInsertionTimeXThan(String oper, String num) {
		log.debug("isInsertionTimeXThan: argument {} operation {}",num,oper);
		BooleanExpression pred = null;

		if (oper.equals("<")) {
			pred = QTag.tag.insertionTime.lt(new Date(new Long(num)));
		} else if (oper.equals(">")) {
			pred = QTag.tag.insertionTime.gt(new Date(new Long(num)));
		} else if (oper.equals(":")) {
			pred = QTag.tag.insertionTime.eq(new Date(new Long(num)));
		}
		return pred;
	}
	/**
	 * @param oper
	 * @param num
	 * @return
	 */
	public static BooleanExpression isModificationTimeXThan(String oper, String num) {
		log.debug("isModificationTimeXThan: argument {} operation {}",num,oper);
		BooleanExpression pred = null;

		if (oper.equals("<")) {
			pred = QTag.tag.modificationTime.lt(new Date(new Long(num)));
		} else if (oper.equals(">")) {
			pred = QTag.tag.modificationTime.gt(new Date(new Long(num)));
		} else if (oper.equals(":")) {
			pred = QTag.tag.modificationTime.eq(new Date(new Long(num)));
		}
		return pred;
	}

	public static Predicate where(BooleanExpression exp) {
		return exp;
	}
}
