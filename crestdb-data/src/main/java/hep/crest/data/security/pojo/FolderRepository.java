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
public interface FolderRepository extends PagingAndSortingRepository<CrestFolders, String>,
        QuerydslPredicateExecutor<CrestFolders> {

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
    void delete(CrestFolders entity);

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.CrudRepository#save(S)
     */
    @SuppressWarnings("unchecked")
    @Override
    CrestFolders save(CrestFolders entity);

    /**
     * @param group
     *            the String
     * @return List<CrestFolders>
     */
    List<CrestFolders> findByGroupRole(@Param("group") String group);

    /**
     * @param schema
     *            the String
     * @return List<CrestFolders>
     */
    List<CrestFolders> findBySchemaName(@Param("schema") String schema);

}
