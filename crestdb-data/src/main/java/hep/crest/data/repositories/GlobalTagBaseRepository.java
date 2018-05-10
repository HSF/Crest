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
 * @author formica
 * @author rsipos
 *
 */
@Transactional(readOnly = true)
public interface GlobalTagBaseRepository extends PagingAndSortingRepository<GlobalTag, String>, QuerydslPredicateExecutor<GlobalTag> {

	@Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.tag "
			+ "WHERE maps.id.globalTagName = (:name)")
	GlobalTag findByNameAndFetchTagsEagerly(@Param("name") String name);

	@Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.tag "
			+ "WHERE maps.id.globalTagName = (:name) and maps.id.record = (:record)")
	GlobalTag findByNameAndFetchRecordTagsEagerly(@Param("name") String name, 
			@Param("record") String record);
	
	@Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.tag "
			+ "WHERE maps.id.globalTagName = (:name) and maps.id.record = (:record) and maps.id.label = (:label) ")
	GlobalTag findByNameAndFetchSpecifiedTagsEagerly(@Param("name") String name, 
			@Param("record") String record, @Param("label") String label);
	
	@Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.tag "
			+ "WHERE maps.id.globalTagName = (:name) and maps.tag.name like (:tag)")
	GlobalTag findByNameAndFilterTagsEagerly(@Param("name") String name, @Param("tag") String tag);
	
	
	GlobalTag findByName(@Param("name") String name);
	
	List<GlobalTag> findByNameLike(@Param("name") String name);

}
