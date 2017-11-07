/**
 *
 */
package hep.crest.data.runinfo.repositories;

import java.math.BigDecimal;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hep.crest.data.repositories.CondDBPageAndSortingRepository;
import hep.crest.data.runinfo.pojo.RunLumiInfo;



/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface RunLumiInfoBaseRepository extends CondDBPageAndSortingRepository<RunLumiInfo, BigDecimal> , QueryDslPredicateExecutor<RunLumiInfo>{

	RunLumiInfo findByRun(@Param("run") BigDecimal run);
		
}
