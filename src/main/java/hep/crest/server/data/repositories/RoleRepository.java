/**
 *
 */
package hep.crest.server.data.repositories;

import hep.crest.server.data.pojo.CrestRoles;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author formica
 *
 */
@Repository
public interface RoleRepository extends CrudRepository<CrestRoles, String> {

}
