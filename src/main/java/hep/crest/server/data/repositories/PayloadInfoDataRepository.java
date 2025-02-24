/**
 *
 */
package hep.crest.server.data.repositories;

import hep.crest.server.data.pojo.PayloadInfoData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Streamer Info.
 *
 * @author formica
 *
 */
@Repository
public interface PayloadInfoDataRepository
        extends CrudRepository<PayloadInfoData, String> {

}
