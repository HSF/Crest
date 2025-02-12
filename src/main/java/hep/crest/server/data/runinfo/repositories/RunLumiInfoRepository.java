/**
 *
 */
package hep.crest.server.data.runinfo.repositories;

import hep.crest.server.data.runinfo.pojo.RunLumiId;
import hep.crest.server.data.runinfo.pojo.RunLumiInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface RunLumiInfoRepository
        extends PagingAndSortingRepository<RunLumiInfo, BigInteger>,
        CrudRepository<RunLumiInfo, BigInteger> {

    /**
     * @param runLumiId
     *            the id (run and lumi block)
     * @return RunLumiInfo
     */
    RunLumiInfo findById(@Param("id") RunLumiId runLumiId);

    /**
     * @param lower
     *            the BigInteger
     * @param upper
     *            the BigInteger
     * @param preq
     *            the PageRequest
     * @return Page<RunLumiInfo>
     */
    @Query("SELECT distinct p FROM RunLumiInfo p "
           + "WHERE p.id.runNumber <= (:upper) "
           + " AND p.id.runNumber >= (:lower) ")
    Page<RunLumiInfo> findByRunNumberInclusive(@Param("lower") BigInteger lower,
                                               @Param("upper") BigInteger upper, Pageable preq);

    /**
     * @param run
     *           the BigInteger
     * @param lower
     *            the BigInteger
     * @param upper
     *            the BigInteger
     * @param preq
     *            the PageRequest
     * @return Page<RunLumiInfo>
     */
    @Query("SELECT distinct p FROM RunLumiInfo p "
           + "WHERE p.id.runNumber=(:run) AND p.id.lb <= (:upper) "
           + " AND p.id.lb >= (:lower)")
    Page<RunLumiInfo> findByLumiBlockInclusive(@Param("run") BigInteger run,
                                               @Param("lower") BigInteger lower,
                                               @Param("upper") BigInteger upper, Pageable preq);

    /**
     * @param lower
     *            the Date
     * @param upper
     *            the Date
     * @param preq
     *            the PageRequest
     * @return Page<RunLumiInfo>
     */
    @Query("SELECT distinct p FROM RunLumiInfo p "
           + "WHERE p.starttime <= (:upper) "
           + "AND p.endtime >= (:lower) "
           + "ORDER BY p.id.runNumber ASC")
    Page<RunLumiInfo> findByDateInclusive(@Param("lower") BigInteger lower, @Param("upper") BigInteger upper,
                                          Pageable preq);
}
