/**
 *
 */
package hep.crest.server.data.repositories;

import hep.crest.server.data.pojo.CrestUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author formica
 *
 */
@Repository
public interface UserRepository extends CrudRepository<CrestUser, String> {

}
