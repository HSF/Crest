/**
 *
 */
package hep.crest.server.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.EmptyPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.swagger.model.GlobalTagDto;
import hep.crest.swagger.model.TagDto;
import ma.glasnost.orika.MapperFacade;


/**
 * @author formica
 * @author rsipos
 *
 */
@Service
public class GlobalTagService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagRepository globalTagRepository;

	@Autowired
	@Qualifier("mapper")
	private MapperFacade mapper;

	/**
	 * @return the globalTagRepository
	 */
	public GlobalTagRepository getGlobalTagRepository() {
		return globalTagRepository;
	}

	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	@SuppressWarnings("unchecked")
	public List<GlobalTagDto> findAllGlobalTags(Predicate qry, Pageable req) throws CdbServiceException {
		try {
			List<GlobalTagDto> dtolist = new ArrayList<>();
			Iterable<GlobalTag> entitylist = null;
			if (qry == null) {
				entitylist = globalTagRepository.findAll(req);
			} else {
				entitylist = globalTagRepository.findAll(qry, req);
			}
			dtolist = StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,GlobalTagDto.class)).collect(Collectors.toList());
			return dtolist;
		} catch (Exception e) {
			throw new CdbServiceException("Cannot find global tag list " + e.getMessage());
		}
	}


	/**
	 * @param globaltagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public GlobalTagDto findOne(String globaltagname) throws CdbServiceException {
		try {
			log.debug("Search for global tag by name " + globaltagname);
			GlobalTag entity = globalTagRepository.findByName(globaltagname);
			return mapper.map(entity,GlobalTagDto.class);
		} catch (Exception e) {
			log.debug("Exception in retrieving global tag list using ByName expression..." + globaltagname);
			throw new CdbServiceException("Cannot find global tag by name: " + e.getMessage());
		}
	}

	private List<TagDto> getTagsFromGT(GlobalTag gt) throws EmptyPojoException {
		if (gt == null) {
			throw new EmptyPojoException("Cannot load tags from a null global tag...");
		}
		Set<GlobalTagMap> gtMaps = gt.getGlobalTagMaps();
		List<TagDto> tagDtos = new ArrayList<>();
		for (GlobalTagMap globalTagMap : gtMaps) {
			tagDtos.add(mapper.map(globalTagMap.getTag(),TagDto.class));
		}
		return tagDtos;
	}

//	/**
//	 * @param globaltagname
//	 * @return the list of TagDtos
//	 * @throws CdbServiceException
//	 */
//	public List<TagDto> getGlobalTagByNameFetchTags(String globaltagname) throws CdbServiceException {
//		try {
//			log.debug("Search for tag list for GT: " + globaltagname);
//			return getTagsFromGT(globalTagRepository.findByNameAndFetchTagsEagerly(globaltagname));
//		} catch (Exception e) {
//			if (e instanceof EmptyPojoException)
//				throw e;
//			log.debug("Exception in retrieving specified tag list for GT: " + globaltagname);
//			throw new CdbServiceException("Cannot find specified tag list for GT: " + e.getMessage());
//		}
//	}
//
//	public List<TagDto> getGlobalTagByNameFetchTags(String globaltagname, String record) throws CdbServiceException {
//		try {
//			log.debug("Search for (record) specified tag list for GT: " + globaltagname);
//			return getTagsFromGT(globalTagRepository.findByNameAndFetchRecordTagsEagerly(globaltagname, record));
//		} catch (Exception e) {
//			log.debug("Exception in retrieving specified tag list with record for GT: " + globaltagname);
//			throw new CdbServiceException("Cannot find specified tag list for GT: " + e.getMessage());
//		}
//
//	}

	public List<TagDto> getGlobalTagByNameFetchTags(String globaltagname, String record, String label)
			throws CdbServiceException {
		try {
			GlobalTag entity = null;
			log.debug("Search for (record, label) specified tag list for GT: " + globaltagname);
			if (record.equals("") && label.equals("")) {
				entity = globalTagRepository.findByNameAndFetchTagsEagerly(globaltagname);
			} else if (label.equals("")) {
				entity = globalTagRepository.findByNameAndFetchRecordTagsEagerly(globaltagname, record);
			} else {
				entity = globalTagRepository.findByNameAndFetchSpecifiedTagsEagerly(globaltagname, record, label);
			}
			
			return getTagsFromGT(entity);
		} catch (Exception e) {
			log.debug("Exception in retrieving specified tag list with record and label for GT: " + globaltagname);
			throw new CdbServiceException("Cannot find specified tag list for GT: " + e.getMessage());
		}

	}

	public GlobalTagDto insertGlobalTag(GlobalTagDto dto) throws CdbServiceException {
		try {
			log.debug("Create global tag from dto " + dto);
			GlobalTag entity =  mapper.map(dto,GlobalTag.class);
			GlobalTag tmpgt = globalTagRepository.findByName(entity.getName());
			if (tmpgt != null) {
				log.debug("Cannot store global tag " + dto+" : resource already exists.. ");
				throw new AlreadyExistsPojoException("Global tag already exists for name "+dto.getName());
			}
			GlobalTag saved = globalTagRepository.save(entity);
			log.debug("Saved entity: " + saved);
			GlobalTagDto dtoentity = mapper.map(saved,GlobalTagDto.class);
			return dtoentity;
		} catch (AlreadyExistsPojoException e) {
			log.debug("Cannot store global tag " + dto+" : resource already exists.. ");
			throw e;
		} catch (ConstraintViolationException e) {
			log.debug("Cannot store global tag " + dto+" : resource already exists ? ");
			throw new AlreadyExistsPojoException("Global tag already exists : " + e.getMessage());
		} catch (Exception e) {
			log.debug("Exception in storing global tag " + dto);
			throw new CdbServiceException("Cannot store global tag : " + e.getMessage());
		}
	}
	
	public GlobalTagDto updateGlobalTag(GlobalTagDto dto) throws CdbServiceException {
		try {
			log.debug("Create global tag from dto " + dto);
			GlobalTag entity =  mapper.map(dto,GlobalTag.class);
			GlobalTag tmpgt = globalTagRepository.findByName(entity.getName());
			if (tmpgt == null) {
				log.debug("Cannot update global tag " + dto+" : resource does not exists.. ");
				throw new NotExistsPojoException("Global tag does not exists for name "+dto.getName());
			}
			GlobalTag saved = globalTagRepository.save(entity);
			log.debug("Saved entity: " + saved);
			GlobalTagDto dtoentity = mapper.map(saved,GlobalTagDto.class);
			return dtoentity;
		} catch (NotExistsPojoException e) {
			log.debug("Cannot store global tag " + dto+" : resource does not exists.. ");
			throw e;
		} catch (ConstraintViolationException e) {
			log.debug("Cannot store global tag " + dto+" : resource does not exists ? ");
			throw new NotExistsPojoException("Global tag does not exists : " + e.getMessage());
		} catch (Exception e) {
			log.debug("Exception in storing global tag " + dto);
			throw new CdbServiceException("Cannot store global tag : " + e.getMessage());
		}
	}

	public void removeGlobalTag(String name) throws CdbServiceException {
		try {
			log.debug("Remove global tag " + name);
			globalTagRepository.delete(name);
			log.debug("Removed entity: " + name);
			return;
		} catch (Exception e) {
			log.debug("Exception in removing global tag " + name);
			throw new CdbServiceException("Cannot remove global tag : " + e.getMessage());
		}
	}
}
