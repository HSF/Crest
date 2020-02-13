/**
 *
 */
package hep.crest.data.runinfo.repositories;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.runinfo.pojo.RunInfo;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface RunInfoBaseRepository
        extends PagingAndSortingRepository<RunInfo, BigDecimal>,
        QuerydslPredicateExecutor<RunInfo> {

    /**
     * @param run
     *            the BigDecimal
     * @return RunInfo
     */
    RunInfo findByRunNumber(@Param("runNumber") BigDecimal run);

    /**
     * @param lower
     *            the BigDecimal
     * @param upper
     *            the BigDecimal
     * @return List<RunInfo>
     */
    @Query("SELECT distinct p FROM RunInfo p "
            + "WHERE p.runNumber <= ("
            + "SELECT min(pi.runNumber) FROM RunInfo pi "
            + "WHERE pi.runNumber >= (:upper)) "
            + "AND p.runNumber >= (:lower)"
            + "ORDER BY p.runNumber ASC")
    List<RunInfo> findByRunNumberInclusive(@Param("lower") BigDecimal lower, @Param("upper") BigDecimal upper);

    /**
     * @param lower
     *            the Date
     * @param upper
     *            the Date
     * @return List<RunInfo>
     */
    @Query("SELECT distinct p FROM RunInfo p "
            + "WHERE p.endTime <= ("
            + "SELECT min(pi.startTime) FROM RunInfo pi "
            + "WHERE pi.startTime >= (:upper)) "
            + "AND p.startTime >= (:lower)"
            + "ORDER BY p.runNumber ASC")
    List<RunInfo> findByDateInclusive(@Param("lower") Date lower, @Param("upper") Date upper);

}
