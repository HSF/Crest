/**
 *
 */
package hep.crest.data.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.pojo.GlobalTagMap;

/**
 * Repository for mappings.
 * 
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface GlobalTagMapBaseRepository
        extends PagingAndSortingRepository<GlobalTagMap, String> {

    /**
     * @param gtag
     *            the String
     * @return List<GlobalTagMap>
     */
    @Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g "
            + "JOIN FETCH p.tag t WHERE p.globalTag.name = (:globaltag)")
    List<GlobalTagMap> findByGlobalTagName(@Param("globaltag") String gtag);

    /**
     * @param tag
     *            the String
     * @return List<GlobalTagMap>
     */
    @Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g "
            + "JOIN FETCH p.tag t WHERE t.name = (:tag)")
    List<GlobalTagMap> findByTagName(@Param("tag") String tag);

}
