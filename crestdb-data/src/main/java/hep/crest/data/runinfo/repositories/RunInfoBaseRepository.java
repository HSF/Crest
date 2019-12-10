/**
 *
 */
package hep.crest.data.runinfo.repositories;

import java.math.BigDecimal;

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
     * @return RunLumiInfo
     */
    RunInfo findByRunNumber(@Param("runNumber") BigDecimal run);

}
