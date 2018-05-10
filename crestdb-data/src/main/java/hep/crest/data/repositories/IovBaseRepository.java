/**
 *
 */
package hep.crest.data.repositories;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.IovId;



/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface IovBaseRepository extends PagingAndSortingRepository<Iov, IovId>,  QuerydslPredicateExecutor<Iov> {

	List<Iov> findByIdTagName(@Param("name") String name);
	
	List<Iov> findByIdTagName(@Param("name") String name, Pageable pageable);

	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:name) and p.id.since = :since and p.payloadHash = (:hash)")
	Iov findBySinceAndTagNameAndHash(@Param("name") String name, @Param("since") BigDecimal since, @Param("hash") String hash);
	
	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:name) and p.id.since >= :since AND  p.id.since < :until "
			+ "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
	List<Iov> selectLatestByGroup(@Param("name") String name, @Param("since") BigDecimal since, @Param("until") BigDecimal until);
	
	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:name) AND p.id.since >= :since AND  p.id.since < :until  AND p.id.insertionTime <= :snap "
			+ "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
	List<Iov> selectSnapshotByGroup(@Param("name") String name, @Param("since") BigDecimal since, @Param("until") BigDecimal until, @Param("snap") Date snapshot);

	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:name) "
			+ "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
	List<Iov> selectLatestByTag(@Param("name") String name);
	
	@Query("SELECT distinct p FROM Iov p "
			+ "WHERE p.id.tagName = (:tagname) AND p.id.insertionTime <= :snap "
			+ "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
	List<Iov> selectSnapshot(@Param("tagname") String tagname, @Param("snap") Date snapshot);
		
}
