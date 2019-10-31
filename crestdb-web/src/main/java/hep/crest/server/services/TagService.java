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

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Repository.
     */
    @Autowired
    private TagRepository tagRepository;

    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    /**
     * @param tagname
     *            the String
     * @return boolean
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public boolean exists(String tagname) throws CdbServiceException {
        try {
            log.debug("Search for tag by name if exists: {}", tagname);
            return tagRepository.existsById(tagname);
        }
        catch (final Exception e) {
            log.error("Exception in checking if tag exists...{}", tagname);
            throw new CdbServiceException("Cannot decide tag existence: " + e.getMessage());
        }
    }

    /**
     * @return long
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public long count() throws CdbServiceException {
        try {
            log.debug("Search for tag count...");
            return tagRepository.count();
        }
        catch (final Exception e) {
            log.error("Exception in retrieving tag count...");
            throw new CdbServiceException("Cannot retreive tag count: " + e.getMessage());
        }
    }

    /**
     * @param id
     *            the String
     * @return TagDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public TagDto findOne(String id) throws CdbServiceException {
        try {
            log.debug("Search for tag by Id...{}", id);
            final Optional<Tag> entity = tagRepository.findById(id);
            if (entity.isPresent()) {
                return mapper.map(entity.get(), TagDto.class);
            }
        }
        catch (final Exception e) {
            log.error("Exception in retrieving tag by id...");
            throw new CdbServiceException("Cannot retreive tag by id: " + e.getMessage());
        }
        return null; // This will trigger a response 404
    }

    /**
     * @param ids
     *            the Iterable<String>
     * @return List<TagDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<TagDto> findAllTags(Iterable<String> ids) throws CdbServiceException {
        try {
            log.debug("Search for all tags by Id list...");
            final Iterable<Tag> entitylist = tagRepository.findAllById(ids);
            return StreamSupport.stream(entitylist.spliterator(), false)
                    .map(s -> mapper.map(s, TagDto.class)).collect(Collectors.toList());
        }
        catch (final Exception e) {
            throw new CdbServiceException("Cannot find tag list by Id list" + e.getMessage());
        }
    }

    /**
     * @param qry
     *            the Predicate
     * @param req
     *            the Pageable
     * @return List<TagDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<TagDto> findAllTags(Predicate qry, Pageable req) throws CdbServiceException {
        try {
            Iterable<Tag> entitylist = null;
            if (qry == null) {
                entitylist = tagRepository.findAll(req);
            }
            else {
                entitylist = tagRepository.findAll(qry, req);
            }
            return StreamSupport.stream(entitylist.spliterator(), false)
                    .map(s -> mapper.map(s, TagDto.class)).collect(Collectors.toList());
        }
        catch (final Exception e) {
            throw new CdbServiceException("Cannot find global tag list " + e.getMessage());
        }
    }

    /**
     * @param dto
     *            the TagDto
     * @return TagDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @throws AlreadyExistsPojoException
     *             If an Exception occurred because pojo exists
     */
    @Transactional
    public TagDto insertTag(TagDto dto) throws CdbServiceException, AlreadyExistsPojoException {
        try {
            log.debug("Create tag from dto {}", dto);
            final Tag entity = mapper.map(dto, Tag.class);
            final Optional<Tag> tmpt = tagRepository.findById(entity.getName());
            if (tmpt.isPresent()) {
                log.debug("Cannot store tag {} : resource already exists.. ", dto);
                throw new AlreadyExistsPojoException(
                        "Tag already exists for name " + dto.getName());
            }
            final Tag saved = tagRepository.save(entity);
            log.debug("Saved entity: {}", saved);
            return mapper.map(saved, TagDto.class);
        }
        catch (final AlreadyExistsPojoException e) {
            log.error("Exception in storing tag {}: resource already exists", dto);
            throw e;
        }
        catch (final Exception e) {
            log.error("Exception in storing tag {}", dto);
            throw new CdbServiceException("Cannot store tag : " + e.getMessage());
        }
    }

    /**
     * Update an existing tag.
     * 
     * @param dto
     *            the TagDto
     * @return TagDto of the updated entity.
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public TagDto updateTag(TagDto dto) throws CdbServiceException {
        try {
            log.debug("Update tag from dto {}", dto);
            final Tag entity = mapper.map(dto, Tag.class);
            final Optional<Tag> tmpt = tagRepository.findById(entity.getName());
            if (!tmpt.isPresent()) {
                log.debug("Cannot update tag {} : resource does not exists.. ", dto);
                throw new CdbServiceException("Tag does not exists for name " + dto.getName());
            }
            final Tag toupd = tmpt.get();
            toupd.setDescription(dto.getDescription());
            toupd.setObjectType(dto.getPayloadSpec());
            toupd.setSynchronization(dto.getSynchronization());
            toupd.setEndOfValidity(dto.getEndOfValidity());
            toupd.setLastValidatedTime(dto.getLastValidatedTime());
            toupd.setTimeType(dto.getTimeType());
            final Tag saved = tagRepository.save(toupd);
            log.debug("Updated entity: {}", saved);
            return mapper.map(saved, TagDto.class);
        }
        catch (final Exception e) {
            log.error("Exception in storing tag {}", dto);
            throw new CdbServiceException("Cannot store tag : " + e.getMessage());
        }
    }

    /**
     * @param name
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public void removeTag(String name) throws CdbServiceException {
        try {
            log.debug("Remove tag {}", name);
            tagRepository.deleteById(name);
            log.debug("Removed entity: {}", name);
        }
        catch (final Exception e) {
            log.error("Exception in removing tag {}", name);
            throw new CdbServiceException("Cannot remove tag : " + e.getMessage());
        }
    }

}
