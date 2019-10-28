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

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.CrudRepository#deleteById(java.lang.
     * Object)
     */
    @Override
    void deleteById(String id);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
     */
    @Override
    void delete(CrestRoles entity);

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.CrudRepository#save(S)
     */
    @SuppressWarnings("unchecked")
    @Override
    CrestRoles save(CrestRoles entity);

}
