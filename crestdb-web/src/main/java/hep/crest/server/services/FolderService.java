/**
 *
 */
package hep.crest.server.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.security.pojo.CrestFolders;
import hep.crest.data.security.pojo.FolderRepository;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.swagger.model.FolderDto;
import ma.glasnost.orika.MapperFacade;

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
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    /**
     * @param qry
     *            the Predicate
     * @param req
     *            the Pageable
     * @return List<FolderDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<FolderDto> findAllFolders(Predicate qry, Pageable req) throws CdbServiceException {
        Iterable<CrestFolders> entitylist = null;
        if (qry == null) {
            entitylist = folderRepository.findAll(req);
        }
        else {
            entitylist = folderRepository.findAll(qry, req);
        }
        
        return StreamSupport.stream(entitylist.spliterator(), false)
                .map(s -> mapper.map(s, FolderDto.class)).collect(Collectors.toList());

    }

    /**
     * @param dto
     *            the FolderDto
     * @return FolderDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @throws AlreadyExistsPojoException
     *             If an Exception occurred because pojo exists
     */
    @Transactional
    public FolderDto insertFolder(FolderDto dto)
            throws CdbServiceException, AlreadyExistsPojoException {
        try {
            log.debug("Create global tag from dto {}", dto);
            final CrestFolders entity = mapper.map(dto, CrestFolders.class);
            final Optional<CrestFolders> tmpgt = folderRepository
                    .findById(entity.getNodeFullpath());
            if (tmpgt.isPresent()) {
                log.debug("Cannot store folder {}  : resource already exists.. ", dto);
                throw new AlreadyExistsPojoException(
                        "Folder already exists for name " + dto.getNodeFullpath());
            }
            log.debug("Saving folder entity {}", entity);
            final CrestFolders saved = folderRepository.save(entity);
            log.trace("Saved entity: {}", saved);
            return mapper.map(saved, FolderDto.class);
        }
        catch (final AlreadyExistsPojoException e) {
            log.error("Cannot store folder {}  : resource already exists.. ", dto);
            throw e;
        }
    }
}
