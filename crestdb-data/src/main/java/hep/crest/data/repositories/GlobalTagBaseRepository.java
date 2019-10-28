/**
 *
 */
package hep.crest.data.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.pojo.GlobalTag;

/**
 * Repository for global tags.
 * 
 * @author formica
 * @author rsipos
 *
 */
@Transactional(readOnly = true)
public interface GlobalTagBaseRepository extends PagingAndSortingRepository<GlobalTag, String>,
        QuerydslPredicateExecutor<GlobalTag> {

    /**
     * @param name
     *            the String
     * @return GlobalTag
     */
    @Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.tag "
            + "WHERE maps.id.globalTagName = (:name)")
    GlobalTag findByNameAndFetchTagsEagerly(@Param("name") String name);

    /**
     * @param name
     *            the String
     * @param record
     *            the String
     * @return GlobalTag
     */
    @Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.tag "
            + "WHERE maps.id.globalTagName = (:name) and maps.id.record = (:record)")
    GlobalTag findByNameAndFetchRecordTagsEagerly(@Param("name") String name,
            @Param("record") String record);

    /**
     * @param name
     *            the String
     * @param record
     *            the String
     * @param label
     *            the String
     * @return GlobalTag
     */
    @Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.tag "
            + "WHERE maps.id.globalTagName = (:name) and maps.id.record = (:record) and maps.id.label = (:label) ")
    GlobalTag findByNameAndFetchSpecifiedTagsEagerly(@Param("name") String name,
            @Param("record") String record, @Param("label") String label);

    /**
     * @param name
     *            the String
     * @param tag
     *            the String
     * @return GlobalTag
     */
    @Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.tag "
            + "WHERE maps.id.globalTagName = (:name) and maps.tag.name like (:tag)")
    GlobalTag findByNameAndFilterTagsEagerly(@Param("name") String name, @Param("tag") String tag);

    /**
     * @param name
     *            the String
     * @return GlobalTag
     */
    GlobalTag findByName(@Param("name") String name);

    /**
     * @param name
     *            the String
     * @return List<GlobalTag>
     */
    List<GlobalTag> findByNameLike(@Param("name") String name);

}
