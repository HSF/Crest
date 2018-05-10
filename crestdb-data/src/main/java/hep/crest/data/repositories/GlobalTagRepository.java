/**
 *
 */
package hep.crest.data.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hep.crest.data.pojo.GlobalTag;



/**
 * @author formica
 *
 */
@Repository
public interface GlobalTagRepository extends CrudRepository<GlobalTag, String>, GlobalTagBaseRepository {

    @Override
    void deleteById(String id);

    @Override
    void delete(GlobalTag entity);

	@SuppressWarnings("unchecked")
	@Override
    GlobalTag save(GlobalTag entity);

}
