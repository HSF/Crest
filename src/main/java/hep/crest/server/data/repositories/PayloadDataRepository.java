/**
 *
 */
package hep.crest.server.data.repositories;

import hep.crest.server.data.pojo.PayloadData;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for Payload DATA.
 *
 * @author formica
 *
 */
public interface PayloadDataRepository
        extends CrudRepository<PayloadData, String>, PayloadDataRepositoryCustom {

}
