/**
 *
 */
package hep.crest.server.services;

import java.util.ArrayList;
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

	
	/***
	 * Implementing CondDBPageAndSortingRepository related functions to retreive TagDtos.
	 * */
	
	public boolean exists(String tagname) throws CdbServiceException {
		try {
			log.debug("Search for tag by name if exists: " + tagname);
			return tagRepository.existsById(tagname);
		} catch (Exception e) {
			log.debug("Exception in retrieving tag existence..."+tagname);
			throw new CdbServiceException("Cannot decide tag existence: " + e.getMessage());
		}
	}
	
	public long count() throws CdbServiceException {
		try {
			log.debug("Search for tag count...");
			return tagRepository.count();
		} catch (Exception e) {
			log.debug("Exception in retrieving tag count...");
			throw new CdbServiceException("Cannot retreive tag count: " + e.getMessage());
		}
	}
	
	public TagDto findOne(String id) throws CdbServiceException {
		try {
			log.debug("Search for tag by Id...");
			Optional<Tag> entity = tagRepository.findById(id);
			if (entity.isPresent()) {
				return mapper.map(entity.get(),TagDto.class);
			}
			throw new CdbServiceException("Entity for tag "+id+" not present...");
		} catch (Exception e) {
			log.debug("Exception in retrieving tag by id...");
			throw new CdbServiceException("Cannot retreive tag by id: " + e.getMessage());
		}
	}
	
	
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	@SuppressWarnings("unchecked")
	public List<TagDto> findAllTags(Iterable<String> ids) throws CdbServiceException {
		try {
			log.debug("Search for all tags by Id list...");
			List<TagDto> dtolist = new ArrayList<>();
			Iterable<Tag> entitylist = tagRepository.findAllById(ids);
			dtolist = StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,TagDto.class)).collect(Collectors.toList());
			return dtolist;
			
		} catch (Exception e) {
			throw new CdbServiceException("Cannot find tag list by Id list" + e.getMessage());
		}
	}
	
	
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	@SuppressWarnings("unchecked")
	public List<TagDto> findAllTags(Predicate qry, Pageable req) throws CdbServiceException {
		try {
			List<TagDto> dtolist = new ArrayList<>();
			Iterable<Tag> entitylist = null;
			if (qry == null) {
				entitylist = tagRepository.findAll(req);
			} else {
				entitylist = tagRepository.findAll(qry, req);
			}
			dtolist = StreamSupport.stream(entitylist.spliterator(), false).map(s -> mapper.map(s,TagDto.class)).collect(Collectors.toList());
			return dtolist;
		} catch (Exception e) {
			throw new CdbServiceException("Cannot find global tag list " + e.getMessage());
		}
	}

	
	@Transactional
	public TagDto insertTag(TagDto dto) throws CdbServiceException {
		try {
			log.debug("Create tag from dto " + dto);
			Tag entity =  mapper.map(dto,Tag.class);
			Optional<Tag> tmpt = tagRepository.findById(entity.getName());
			if (tmpt.isPresent()) {
				log.debug("Cannot store tag " + dto+" : resource already exists.. ");
				throw new AlreadyExistsPojoException("Tag already exists for name "+dto.getName());
			}
			Tag saved = tagRepository.save(entity);
			log.debug("Saved entity: " + saved);
			TagDto dtoentity = mapper.map(saved,TagDto.class);
			return dtoentity;
		} catch (AlreadyExistsPojoException e) {
			log.debug("Cannot store tag " + dto+" : resource already exists.. ");
			throw e;
		} catch (Exception e) {
			log.debug("Exception in storing tag " + dto);
			throw new CdbServiceException("Cannot store tag : " + e.getMessage());
		}
	}
	
	public void removeTag(String name) throws CdbServiceException {
		try {
			log.debug("Remove tag " + name);
			tagRepository.deleteById(name);
			log.debug("Removed entity: " + name);
			return;
		} catch (Exception e) {
			log.debug("Exception in removing tag " + name);
			throw new CdbServiceException("Cannot remove tag : " + e.getMessage());
		}
	}
	
	
}
