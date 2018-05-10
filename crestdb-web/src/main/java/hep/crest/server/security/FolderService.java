/**
 *
 */
package hep.crest.server.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.repositories.GlobalTagRepository;
import hep.crest.data.security.pojo.CrestFolders;
import hep.crest.data.security.pojo.FolderRepository;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.EmptyPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.swagger.model.FolderDto;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.TagDto;
import ma.glasnost.orika.MapperFacade;


/**
 * @author formica
 * @author rsipos
 *
 */
@Service
public class FolderService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	@Qualifier("mapper")
	private MapperFacade mapper;


	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	@SuppressWarnings("unchecked")
	public List<FolderDto> findAllFolders(Predicate qry, Pageable req) throws CdbServiceException {
		try {
			List<FolderDto> dtolist = new ArrayList<>();
			Iterable<CrestFolders> entitylist = null;
			if (qry == null) {
				entitylist = folderRepository.findAll(req);
			} else {
				entitylist = folderRepository.findAll(qry, req);
			}
			dtolist = StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,FolderDto.class)).collect(Collectors.toList());
			return dtolist;
		} catch (Exception e) {
			throw new CdbServiceException("Cannot find folder list " + e.getMessage());
		}
	}



	@Transactional
	public FolderDto insertFolder(FolderDto dto) throws CdbServiceException {
		try {
			log.debug("Create global tag from dto " + dto);
			CrestFolders entity =  mapper.map(dto,CrestFolders.class);
			Optional<CrestFolders> tmpgt = folderRepository.findById(entity.getNodeFullpath());
			if (tmpgt.isPresent()) {
				log.debug("Cannot store folder " + dto+" : resource already exists.. ");
				throw new AlreadyExistsPojoException("Folder already exists for name "+dto.getNodeFullpath());				
			}
			log.debug("Saving folder entity " + entity);
			CrestFolders saved = folderRepository.save(entity);
			log.debug("Saved entity: " + saved);
			FolderDto dtoentity = mapper.map(saved,FolderDto.class);
			return dtoentity;
		} catch (AlreadyExistsPojoException e) {
			log.debug("Cannot store folder " + dto+" : resource already exists.. ");
			throw e;
		} catch (ConstraintViolationException e) {
			log.debug("Cannot store folder " + dto+" : resource already exists ? ");
			throw new AlreadyExistsPojoException("Folder already exists : " + e.getMessage());
		} catch (Exception e) {
			log.debug("Exception in storing folder " + dto);
			throw new CdbServiceException("Cannot store folder : " + e.getMessage());
		}
	}
}
