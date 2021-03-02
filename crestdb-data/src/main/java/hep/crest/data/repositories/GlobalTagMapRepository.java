/**
 *
 */
package hep.crest.data.repositories;

import java.util.List;

import hep.crest.data.pojo.GlobalTagMapId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.pojo.GlobalTagMap;

/**
 * Repository for mappings.
 *
 * @author formica
 */
@Repository
@Transactional(readOnly = true)
public interface GlobalTagMapRepository extends PagingAndSortingRepository<GlobalTagMap, GlobalTagMapId> {

    /**
     * @param gtag the String
     * @return List<GlobalTagMap>
     */
    @Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g "
            + "JOIN FETCH p.tag t WHERE g.name = (:globaltag)")
    List<GlobalTagMap> findByGlobalTagName(@Param("globaltag") String gtag);

    /**
     * Find by global tag name and label and tag name like.
     *
     * @param gtag the gtag
     * @param label the label
     * @param tag the tag
     * @return the list
     */
    @Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g "
            + "JOIN FETCH p.tag t WHERE g.name = (:globaltag) AND p.id.label = (:label) AND t.name like (:tag)")
    List<GlobalTagMap> findByGlobalTagNameAndLabelAndTagNameLike(@Param("globaltag") String gtag,
            @Param("label") String label, @Param("tag") String tag);

    /**
     * @param tag the String
     * @return List<GlobalTagMap>
     */
    @Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g "
            + "JOIN FETCH p.tag t WHERE t.name = (:tag)")
    List<GlobalTagMap> findByTagName(@Param("tag") String tag);

}
