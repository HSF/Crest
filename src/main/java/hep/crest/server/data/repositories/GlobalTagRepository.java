/**
 *
 */
package hep.crest.server.data.repositories;

import hep.crest.server.data.pojo.GlobalTag;
import hep.crest.server.exceptions.AbstractCdbServiceException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for global tags.
 *
 * @author formica
 * @author rsipos
 *
 */
@Repository
public interface GlobalTagRepository
        extends PagingAndSortingRepository<GlobalTag, String>,
        CrudRepository<GlobalTag, String>,
        GlobalTagRepositoryCustom {


    /**
     * @param name
     *            the String
     * @return Optional<GlobalTag>
     * @throws AbstractCdbServiceException
     *             If an Exception occurred
     */
    Optional<GlobalTag> findByName(@Param("name") String name) throws AbstractCdbServiceException;

    /**
     * @param name
     *            the String
     * @return List<GlobalTag>
     */
    List<GlobalTag> findByNameLike(@Param("name") String name);

}
