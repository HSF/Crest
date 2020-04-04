/**
 *
 */
package hep.crest.data.repositories;

import hep.crest.data.pojo.GlobalTagMapId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hep.crest.data.pojo.GlobalTagMap;

/**
 * @author formica
 *
 */
@Repository
public interface GlobalTagMapRepository
        extends CrudRepository<GlobalTagMap, GlobalTagMapId>, GlobalTagMapBaseRepository {

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.CrudRepository#deleteById(java.lang.
     * Object)
     */
    @Override
    void deleteById(GlobalTagMapId id);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
     */
    @Override
    void delete(GlobalTagMap entity);

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.CrudRepository#save(S)
     */
    @SuppressWarnings("unchecked")
    @Override
    GlobalTagMap save(GlobalTagMap entity);

}
