/**
 *
 */
package hep.crest.data.repositories;

import hep.crest.data.pojo.Tag;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
@Repository
public interface TagRepository
        extends PagingAndSortingRepository<Tag, String>, QuerydslPredicateExecutor<Tag>, ITagQuery {

}
