/**
 *
 */
package hep.crest.data.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.IovId;


/**
 * @author formica
 *
 */
@Repository
public interface IovRepository extends CrudRepository<Iov, IovId>, IovBaseRepository {

    @Override
    void delete(IovId id);

    @Override
    void delete(Iov entity);

    @SuppressWarnings("unchecked")
	@Override
    Iov save(Iov entity);

}
