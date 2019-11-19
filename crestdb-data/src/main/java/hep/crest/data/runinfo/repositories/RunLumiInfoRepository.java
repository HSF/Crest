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
public interface RunLumiInfoRepository
        extends CrudRepository<RunLumiInfo, BigDecimal>, RunLumiInfoBaseRepository {

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.CrudRepository#deleteById(java.lang.
     * Object)
     */
    @Override
    void deleteById(BigDecimal id);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
     */
    @Override
    void delete(RunLumiInfo entity);

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.CrudRepository#save(S)
     */
    @SuppressWarnings("unchecked")
    @Override
    RunLumiInfo save(RunLumiInfo entity);

}
