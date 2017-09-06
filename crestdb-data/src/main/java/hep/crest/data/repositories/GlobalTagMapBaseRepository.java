/**
 *
 */
package hep.crest.data.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.pojo.GlobalTagMap;



/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface GlobalTagMapBaseRepository extends CondDBPageAndSortingRepository<GlobalTagMap, String> {

	@Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g JOIN FETCH p.tag t WHERE p.globalTag.name = (:globaltag)")
	List<GlobalTagMap> findByGlobalTagName(@Param("globaltag")String gtag);
	
	@Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g JOIN FETCH p.tag t WHERE t.name = (:tag)")
	List<GlobalTagMap> findByTagName(@Param("tag")String tag);

}
