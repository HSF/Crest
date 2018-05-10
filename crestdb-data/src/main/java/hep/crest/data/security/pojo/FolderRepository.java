/**
 *
 */
package hep.crest.data.security.pojo;

import java.util.List;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * @author formica
 *
 */
@Repository
public interface FolderRepository extends PagingAndSortingRepository<CrestFolders, String>, QuerydslPredicateExecutor<CrestFolders> {

    @Override
    void deleteById(String id);

    @Override
    void delete(CrestFolders entity);

    @SuppressWarnings("unchecked")
	@Override
	CrestFolders save(CrestFolders entity);

	List<CrestFolders> findByGroupRole(@Param("group") String group);

	List<CrestFolders> findBySchemaName(@Param("schema") String schema);

}
