/**
 *
 */
package hep.crest.data.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hep.crest.data.pojo.GlobalTagMap;



/**
 * @author formica
 *
 */
@Repository
public interface GlobalTagMapRepository extends CrudRepository<GlobalTagMap, String>, GlobalTagMapBaseRepository {

    @Override
    void delete(String id);

    @Override
    void delete(GlobalTagMap entity);

    @SuppressWarnings("unchecked")
	@Override
    GlobalTagMap save(GlobalTagMap entity);

}
