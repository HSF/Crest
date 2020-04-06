/**
 *
 */
package hep.crest.server.services;

import com.querydsl.core.types.Predicate;
import hep.crest.data.security.pojo.CrestFolders;
import hep.crest.data.security.pojo.FolderRepository;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
public class FolderService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FolderService.class);

    /**
     * Repository.
     */
    @Autowired
    private FolderRepository folderRepository;

    /**
     * @param qry
     *            the Predicate
     * @param req
     *            the Pageable
     * @return Iterable<CrestFolders>
     */
    public Iterable<CrestFolders> findAllFolders(Predicate qry, Pageable req) {
        Iterable<CrestFolders> entitylist = null;
        if (qry == null) {
            if (req == null) {
                entitylist = folderRepository.findAll();
            }
            else {
                entitylist = folderRepository.findAll(req);
            }
        }
        else {
            entitylist = folderRepository.findAll(qry, req);
        }
        return entitylist;
    }

    /**
     * @param entity
     *            the CrestFolders
     * @return CrestFolders
     * @throws AlreadyExistsPojoException
     *             If an Exception occurred because pojo exists
     */
    @Transactional
    public CrestFolders insertFolder(CrestFolders entity)
            throws AlreadyExistsPojoException {
        log.debug("Create CrestFolder from  {}", entity);
        final Optional<CrestFolders> tmpgt = folderRepository
                .findById(entity.getNodeFullpath());
        if (tmpgt.isPresent()) {
            log.debug("Cannot store folder {}  : resource already exists.. ", entity);
            throw new AlreadyExistsPojoException(
                    "Folder already exists for name " + entity.getNodeFullpath());
        }
        log.debug("Saving folder entity {}", entity);
        final CrestFolders saved = folderRepository.save(entity);
        log.trace("Saved entity: {}", saved);
        return saved;
    }
}
