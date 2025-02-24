/**
 *
 */
package hep.crest.server.services;

import hep.crest.server.data.pojo.CrestFolders;
import hep.crest.server.data.repositories.CrestFoldersRepository;
import hep.crest.server.exceptions.AbstractCdbServiceException;
import hep.crest.server.exceptions.ConflictException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * A service to handle folders. This is not yet used in CMS nor ATLAS
 * implementation.
 *
 * @author formica
 * @author rsipos
 *
 */
@Service
@Slf4j
public class FolderService {

    /**
     * Repository.
     */
    private CrestFoldersRepository crestFoldersRepository;

    /**
     * Ctor with injection.
     * @param crestFoldersRepository
     */
    @Autowired
    public FolderService(CrestFoldersRepository crestFoldersRepository) {
        this.crestFoldersRepository = crestFoldersRepository;
    }

    /**
     * @param entity
     *            the CrestFolders
     * @return CrestFolders
     * @throws AbstractCdbServiceException
     *             If an Exception occurred because pojo exists
     */
    @Transactional
    public CrestFolders insertFolder(CrestFolders entity) throws AbstractCdbServiceException {
        log.debug("Create CrestFolder from  {}", entity);
        final Optional<CrestFolders> tmpgt = crestFoldersRepository
                .findById(entity.getNodeFullpath());
        if (tmpgt.isPresent()) {
            log.debug("Cannot store folder {}  : resource already exists.. ", entity);
            throw new ConflictException(
                    "Folder already exists for name " + entity.getNodeFullpath());
        }
        log.debug("Saving folder entity {}", entity);
        final CrestFolders saved = crestFoldersRepository.save(entity);
        log.trace("Saved entity: {}", saved);
        return saved;
    }

    /**
     *
     * @param schema
     * @return List of CrestFolders
     */
    public List<CrestFolders> findFoldersBySchema(String schema) {
        return crestFoldersRepository.findBySchemaName(schema);
    }
}
