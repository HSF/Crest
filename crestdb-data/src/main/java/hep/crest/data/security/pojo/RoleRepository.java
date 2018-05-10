/**
 *
 */
package hep.crest.data.security.pojo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


/**
 * @author formica
 *
 */
@Repository
public interface RoleRepository extends CrudRepository<CrestRoles, String> {

    @Override
    void deleteById(String id);

    @Override
    void delete(CrestRoles entity);

    @SuppressWarnings("unchecked")
	@Override
	CrestRoles save(CrestRoles entity);

}
