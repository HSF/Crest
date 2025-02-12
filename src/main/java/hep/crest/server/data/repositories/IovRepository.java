/**
 *
 */
package hep.crest.server.data.repositories;

import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.IovId;
import hep.crest.server.exceptions.AbstractCdbServiceException;
import hep.crest.server.exceptions.CdbNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * Repository for IOVs.
 *
 * @author formica
 *
 */
@Repository
public interface IovRepository
        extends JpaRepository<Iov, IovId>,
        CrudRepository<Iov, IovId>,
        IovRepositoryCustom {

    /**
     * Retrieve all iovs for a tag. Used when deleting.
     *
     * @param name
     * @param preq the Page request
     * @return List of Iovs
     */
    Page<Iov> findByIdTagName(@Param("name") String name, Pageable preq);

    /**
     * Retrieve IOVs as a stream. Useful to process large amount of data.
     *
     * @param tagName
     * @return Stream of Iov
     */
    @Query("SELECT i FROM Iov i WHERE i.id.tagName = :tagName")
    @Transactional(readOnly = true)
    Stream<Iov> streamByTagName(@Param("tagName") String tagName);

    /**
     *
     * @param tagName
     * @param hash
     * @return int
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Iov i WHERE i.id.tagName = :tagName AND i.payloadHash = :hash")
    int deleteByTagNameAndHash(@Param("tagName") String tagName, @Param("hash") String hash);

    /**
     * Update the insertion time.
     * @param tagName
     * @param since
     * @param hash
     * @param instime
     * @return int
     */
    @Modifying
    @Transactional
    @Query("UPDATE Iov i set i.id.insertionTime = :instime WHERE i.id.tagName = :tagName AND "
            + "i.payloadHash = :hash AND i.id.since = :since")
    int updateIov(@Param("tagName") String tagName,
                  @Param("since") BigInteger since, @Param("hash") String hash,
                  @Param("instime") Date instime);

    /**
     * Retrieve all iovs for a given hash. Used when deleting.
     *
     * @param hash
     * @return List of Iovs
     */
    List<Iov> findByPayloadHash(@Param("hash") String hash);

    /**
     * Check existence of IOV by unique fields.
     * @param name     the String
     * @param since    the BigDecimal
     * @param hash the Hash
     * @return Iov
     * @throws AbstractCdbServiceException
     *             If an Exception occurred
     */
    @Query("SELECT distinct p FROM Iov p "
           + "WHERE p.id.tagName = (:name) AND p.id.since = (:since) AND p.payloadHash = (:hash)")
    Iov exists(@Param("name") String name, @Param("since") BigInteger since, @Param("hash") String hash)
            throws CdbNotFoundException;

    /**
     * @param name     the String
     * @param since    the BigDecimal
     * @param snapshot the Date
     * @return List<Iov>
     * @throws AbstractCdbServiceException
     *             If an Exception occurred
     */
    @Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
           + "WHERE tag.name = (:name) AND p.id.since = ("
           + "SELECT max(pi.id.since) FROM Iov pi JOIN pi.tag pt "
           + "WHERE pt.name = (:name) AND pi.id.since <= :since AND pi.id.insertionTime <= :snap) "
           + "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
    List<Iov> selectAtTime(@Param("name") String name, @Param("since") BigInteger since,
                           @Param("snap") Date snapshot) throws CdbNotFoundException;

    /**
     * @param name     the String
     * @param since    the BigDecimal
     * @param until    the BigDecimal
     * @param snapshot the Date
     * @return List<Iov>
     */
    @Query("SELECT distinct p FROM Iov p JOIN FETCH p.tag tag "
           + "WHERE tag.name = (:name) AND p.id.since >= ("
           + "SELECT max(pi.id.since) FROM Iov pi JOIN pi.tag pt "
           + "WHERE pt.name = (:name) AND pi.id.since <= :since AND pi.id.insertionTime <= :snap) "
           + "AND p.id.since <= :until AND p.id.insertionTime <= :snap "
           + "ORDER BY p.id.since ASC, p.id.insertionTime DESC")
    List<Iov> getRange(@Param("name") String name, @Param("since") BigInteger since,
                       @Param("until") BigInteger until,
                       @Param("snap") Date snapshot);
}
