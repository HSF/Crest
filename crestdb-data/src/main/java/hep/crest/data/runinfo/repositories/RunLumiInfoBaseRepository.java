/**
 *
 */
package hep.crest.data.runinfo.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.runinfo.pojo.RunLumiInfo;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface RunLumiInfoBaseRepository
        extends PagingAndSortingRepository<RunLumiInfo, BigDecimal>,
        QuerydslPredicateExecutor<RunLumiInfo> {

    /**
     * @param run
     *            the BigDecimal
     * @return RunLumiInfo
     */
    RunLumiInfo findByRunNumber(@Param("runNumber") BigDecimal run);

    /**
     * @param lower
     *            the BigDecimal
     * @param upper
     *            the BigDecimal
     * @return List<RunLumiInfo>
     */
    @Query("SELECT distinct p FROM RunLumiInfo p " + "WHERE p.runNumber <= ("
            + "SELECT min(pi.runNumber) FROM RunLumiInfo pi " + "WHERE pi.runNumber >= (:upper)) "
            + "AND p.runNumber >= (:lower)" + "ORDER BY p.runNumber ASC")
    List<RunLumiInfo> findByRunNumberInclusive(@Param("lower") BigDecimal lower,
            @Param("upper") BigDecimal upper);

    /**
     * @param lower
     *            the Date
     * @param upper
     *            the Date
     * @return List<RunLumiInfo>
     */
    @Query("SELECT distinct p FROM RunLumiInfo p "
            + "WHERE p.starttime <= ("
            + "SELECT min(pi.starttime) FROM RunLumiInfo pi "
            + "WHERE pi.starttime >= (:upper)) "
            + "AND p.endtime >= (:lower)"
            + "ORDER BY p.runNumber ASC")
    List<RunLumiInfo> findByDateInclusive(@Param("lower") BigDecimal lower, @Param("upper") BigDecimal upper);
}
