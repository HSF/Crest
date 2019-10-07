/**
 *
 */
package hep.crest.data.repositories;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
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

	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag t "
			+ "WHERE p.id.tagid = (:id) ")
	List<Iov> findByIdTagid(@Param("id") Long id);
	
	@Query(value="SELECT distinct p FROM Iov p JOIN FETCH p.tag t "
			+ "WHERE p.id.tagid = (:id) ",countQuery = "SELECT COUNT(c) FROM Iov c JOIN c.tag WHERE c.id.tagid = (:id)")
	Page<Iov> findByIdTagid(@Param("id") Long id, Pageable pageable);

	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:name) and p.id.since = :since and p.payloadHash = (:hash)")
	Iov findBySinceAndTagNameAndHash(@Param("name") String name, @Param("since") BigDecimal since, @Param("hash") String hash);

	@Query("SELECT distinct p FROM Iov p "
			+ "WHERE p.id.tagid = (:id) and p.id.since = :since and p.payloadHash = (:hash)")
	Iov findBySinceAndTagidAndHash(@Param("id") Long id, @Param("since") BigDecimal since, @Param("hash") String hash);

	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:name) and p.id.since >= :since AND  p.id.since < :until "
			+ "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
	List<Iov> selectLatestByGroup(@Param("name") String name, @Param("since") BigDecimal since, @Param("until") BigDecimal until);
	
	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:name) AND p.id.since >= :since AND  p.id.since < :until  AND p.id.insertionTime <= :snap "
			+ "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
	List<Iov> selectSnapshotByGroup(@Param("name") String name, @Param("since") BigDecimal since, @Param("until") BigDecimal until, @Param("snap") Date snapshot);

	//FIXME : complete as NATIVE request: @Query(nativeQuery = true)
	/**
	 * Native query example:
	 * select * from IOV where 
		TAG_NAME = 'MuonAlignMDTBarrelAlign-RUN2-BA_ROLLING_11-BLKP-UPD4-00' AND since = (
		select max(SINCE) from IOV 
		    where TAG_NAME = 'MuonAlignMDTBarrelAlign-RUN2-BA_ROLLING_11-BLKP-UPD4-00'
        AND SINCE<=1433565235500000000 )
	 */
	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:name) AND p.id.since = ("
			+ "SELECT max(pi.id.since) FROM Iov pi JOIN pi.tag pt "
			+ "WHERE pt.name = (:name) AND pi.id.since <= :since AND pi.id.insertionTime <= :snap) "
			+ "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
	List<Iov> selectAtTime(@Param("name") String name, @Param("since") BigDecimal since, @Param("snap") Date snapshot);

	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:name) "
			+ "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
	List<Iov> selectLatestByTag(@Param("name") String name);
	
	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
			+ "WHERE tag.name = (:tagname) AND p.id.insertionTime <= :snap "
			+ "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
	List<Iov> selectSnapshot(@Param("tagname") String tagname, @Param("snap") Date snapshot);
		
}
