/**
 *
 */
package hep.crest.data.repositories;

import java.util.List;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.pojo.Tag;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface TagBaseRepository
        extends PagingAndSortingRepository<Tag, String>, QuerydslPredicateExecutor<Tag> {

    /**
     * @param name
     *            the String
     * @return Tag
     */
    Tag findByName(@Param("name") String name);

    /**
     * @param name
     *            the String
     * @return List<Tag>
     */
    List<Tag> findByNameLike(@Param("name") String name);

}
