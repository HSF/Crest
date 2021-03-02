/**
 *
 */
package hep.crest.data.repositories;

import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.IovId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for IOVs.
 *
 * @author formica
 *
 */
@Repository
@Transactional(readOnly = true)
public interface IovRepository
        extends PagingAndSortingRepository<Iov, IovId>, QuerydslPredicateExecutor<Iov>, IIovQuery {

    /**
     * @param name
     *            the String
     * @param pageable
     *            the Pageable
     * @return List<Iov>
     */
    List<Iov> findByIdTagName(@Param("name") String name, Pageable pageable);

}
