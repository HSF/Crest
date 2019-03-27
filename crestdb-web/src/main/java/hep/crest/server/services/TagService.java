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
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.TagRepository;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.swagger.model.TagDto;
import ma.glasnost.orika.MapperFacade;


/**
 * @author rsipos
 *
 */
@Service
public class TagService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TagRepository tagRepository;
	
	@Autowired
	@Qualifier("mapper")
	private MapperFacade mapper;

	
	/**
	 * @param tagname
	 * @return
	 * @throws CdbServiceException
	 */
	public boolean exists(String tagname) throws CdbServiceException {
		try {
			log.debug("Search for tag by name if exists: {}", tagname);
			return tagRepository.existsById(tagname);
		} catch (Exception e) {
			log.error("Exception in checking if tag exists...{}", tagname);
			throw new CdbServiceException("Cannot decide tag existence: " + e.getMessage());
		}
	}
	
	/**
	 * @return
	 * @throws CdbServiceException
	 */
	public long count() throws CdbServiceException {
		try {
			log.debug("Search for tag count...");
			return tagRepository.count();
		} catch (Exception e) {
			log.error("Exception in retrieving tag count...");
			throw new CdbServiceException("Cannot retreive tag count: " + e.getMessage());
		}
	}
	
	/**
	 * @param id
	 * @return
	 * @throws CdbServiceException
	 */
	public TagDto findOne(String id) throws CdbServiceException {
		try {
			log.debug("Search for tag by Id...{}",id);
			Optional<Tag> entity = tagRepository.findById(id);
			if (entity.isPresent()) {
				return mapper.map(entity.get(),TagDto.class);
			}
		} catch (Exception e) {
			log.error("Exception in retrieving tag by id...");
			throw new CdbServiceException("Cannot retreive tag by id: " + e.getMessage());
		}
		return null; // This will trigger a response 404
	}
	
	
	/**
	 * @param ids
	 * @return
	 * @throws CdbServiceException
	 */
	public List<TagDto> findAllTags(Iterable<String> ids) throws CdbServiceException {
		try {
			log.debug("Search for all tags by Id list...");
			Iterable<Tag> entitylist = tagRepository.findAllById(ids);
			return StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,TagDto.class)).collect(Collectors.toList());
		} catch (Exception e) {
			throw new CdbServiceException("Cannot find tag list by Id list" + e.getMessage());
		}
	}
	
	/**
	 * @param qry
	 * @param req
	 * @return
	 * @throws CdbServiceException
	 */
	public List<TagDto> findAllTags(Predicate qry, Pageable req) throws CdbServiceException {
		try {
			Iterable<Tag> entitylist = null;
			if (qry == null) {
				entitylist = tagRepository.findAll(req);
			} else {
				entitylist = tagRepository.findAll(qry, req);
			}
			return StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,TagDto.class)).collect(Collectors.toList());
		} catch (Exception e) {
			throw new CdbServiceException("Cannot find global tag list " + e.getMessage());
		}
	}

	
	/**
	 * @param dto
	 * @return
	 * @throws CdbServiceException
	 */
	@Transactional
	public TagDto insertTag(TagDto dto) throws CdbServiceException {
		try {
			log.debug("Create tag from dto {}", dto);
			Tag entity =  mapper.map(dto,Tag.class);
			Optional<Tag> tmpt = tagRepository.findById(entity.getName());
			if (tmpt.isPresent()) {
				log.debug("Cannot store tag {} : resource already exists.. ",dto);
				throw new AlreadyExistsPojoException("Tag already exists for name "+dto.getName());
			}
			Tag saved = tagRepository.save(entity);
			log.debug("Saved entity: {}", saved);
			return mapper.map(saved,TagDto.class);
		} catch (AlreadyExistsPojoException e) {
			log.error("Exception in storing tag {}: resource already exists",dto);
			throw e;
		} catch (Exception e) {
			log.error("Exception in storing tag {}",dto);
			throw new CdbServiceException("Cannot store tag : " + e.getMessage());
		}
	}
	
	/**
	 * Update an existing tag
	 * @param dto
	 * @return
	 * 		TagDto of the updated entity.
	 * @throws CdbServiceException
	 */
	@Transactional
	public TagDto updateTag(TagDto dto) throws CdbServiceException {
		try {
			log.debug("Update tag from dto {}", dto);
			Tag entity =  mapper.map(dto,Tag.class);
			Optional<Tag> tmpt = tagRepository.findById(entity.getName());
			if (!tmpt.isPresent()) {
				log.debug("Cannot update tag {} : resource does not exists.. ",dto);
				throw new CdbServiceException("Tag does not exists for name "+dto.getName());
			}
			Tag toupd = tmpt.get();
			toupd.setDescription(dto.getDescription());
			toupd.setObjectType(dto.getObjectType());
			toupd.setSynchronization(dto.getSynchronization());
			toupd.setEndOfValidity(dto.getEndOfValidity());
			toupd.setLastValidatedTime(dto.getLastValidatedTime());
			toupd.setTimeType(dto.getTimeType());
			Tag saved = tagRepository.save(toupd);
			log.debug("Updated entity: {}", saved);
			return mapper.map(saved,TagDto.class);
		} catch (Exception e) {
			log.error("Exception in storing tag {}",dto);
			throw new CdbServiceException("Cannot store tag : " + e.getMessage());
		}
	}

	/**
	 * @param name
	 * @throws CdbServiceException
	 */
	@Transactional
	public void removeTag(String name) throws CdbServiceException {
		try {
			log.debug("Remove tag {}",name);
			tagRepository.deleteById(name);
			log.debug("Removed entity: {}",name);
		} catch (Exception e) {
			log.error("Exception in removing tag {}", name);
			throw new CdbServiceException("Cannot remove tag : " + e.getMessage());
		}
	}
	
	
}
