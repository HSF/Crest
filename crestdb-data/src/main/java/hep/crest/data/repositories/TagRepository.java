/**
 *
 */
package hep.crest.data.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hep.crest.data.pojo.Tag;



/**
 * @author formica
 *
 */
@Repository
public interface TagRepository extends CrudRepository<Tag, String>, TagBaseRepository {

    @Override
    void deleteById(String id);

    @Override
    void delete(Tag entity);

    @SuppressWarnings("unchecked")
	@Override
    Tag save(Tag entity);

}
