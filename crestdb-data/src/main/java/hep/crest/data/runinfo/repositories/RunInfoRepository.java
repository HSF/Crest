/**
 *
 */
package hep.crest.data.runinfo.repositories;

import java.math.BigDecimal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hep.crest.data.runinfo.pojo.RunInfo;

/**
 * @author formica
 *
 */
@Repository
public interface RunInfoRepository
        extends CrudRepository<RunInfo, BigDecimal>, RunInfoBaseRepository {

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
    void delete(RunInfo entity);

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.CrudRepository#save(S)
     */
    @SuppressWarnings("unchecked")
    @Override
    RunInfo save(RunInfo entity);

}
