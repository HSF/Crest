/**
 *
 */
package hep.crest.data.runinfo.repositories;

import java.math.BigDecimal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hep.crest.data.runinfo.pojo.RunLumiInfo;


/**
 * @author formica
 *
 */
@Repository
public interface RunLumiInfoRepository extends CrudRepository<RunLumiInfo, BigDecimal>, RunLumiInfoBaseRepository {

    @Override
    void deleteById(BigDecimal id);

    @Override
    void delete(RunLumiInfo entity);

    @SuppressWarnings("unchecked")
	@Override
	RunLumiInfo save(RunLumiInfo entity);

}
