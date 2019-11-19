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
public interface UserRepository extends CrudRepository<CrestUser, String> {

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
    void delete(CrestUser entity);

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.CrudRepository#save(S)
     */
    @SuppressWarnings("unchecked")
    @Override
    CrestUser save(CrestUser entity);

}
