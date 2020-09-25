/**
 *
 */
package hep.crest.server.services;

import com.querydsl.core.types.Predicate;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.TagMetaDataBaseCustom;
import hep.crest.data.repositories.TagRepository;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.swagger.model.TagMetaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author rsipos
 *
 */
@Service
public class TagService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    /**
     * Repository.
     */
    @Autowired
    private TagRepository tagRepository;
    /**
     * Repository.
     */
    @Autowired
    @Qualifier("tagmetarepo")
    private TagMetaDataBaseCustom tagmetaRepository;

    /**
     * @param tagname
     *            the String
     * @return boolean
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public boolean exists(String tagname) throws CdbServiceException {
        try {
            log.debug("Verify existence of Tag->{}", tagname);
            return tagRepository.existsById(tagname);
        }
        catch (final IllegalArgumentException | InvalidDataAccessApiUsageException e) {
            throw new CdbServiceException("Wrong tagname " + tagname, e);
        }
    }

    /**
     * Count the total number of tags. Use with care and do not expose.
     * @return long
     */
    public long count() {
        log.debug("Search for tag count...");
        return tagRepository.count();
    }

    /**
     * @param id
     *            the String representing the Tag name
     * @return Tag
     * @throws NotExistsPojoException
     *             If object was not found
     */
    public Tag findOne(String id) throws NotExistsPojoException {
        try {
            log.debug("Search for tag by Id...{}", id);
            final Optional<Tag> entity = tagRepository.findById(id);
            if (entity.isPresent()) {
                return entity.get();
            }
            throw new NotExistsPojoException(id);
        }
        catch (final IllegalArgumentException | InvalidDataAccessApiUsageException e) {
            log.error("Should never happen, wrong id was used {} : {}", id, e);
        }
        return null;
    }

    /**
     * @param ids
     *            the Iterable<String>
     * @return Iterable<Tag>
     */
    public Iterable<Tag> findAllTags(Iterable<String> ids) {
        log.debug("Search for all tags by Id list...");
        return tagRepository.findAllById(ids);
    }

    /**
     * @param qry
     *            the Predicate
     * @param req
     *            the Pageable
     * @return Iterable<Tag>
     */
    public Iterable<Tag> findAllTags(Predicate qry, Pageable req) {
        Iterable<Tag> entitylist = null;
        if (qry == null) {
            if (req == null) {
                entitylist = tagRepository.findAll();
            }
            else {
                entitylist = tagRepository.findAll(req);
            }
        }
        else {
            entitylist = tagRepository.findAll(qry, req);
        }
        return entitylist;
    }

    /**
     * @param entity
     *            the Tag
     * @return Tag
     * @throws AlreadyExistsPojoException
     *             If an Exception occurred because pojo exists
     */
    @Transactional
    public Tag insertTag(Tag entity) throws AlreadyExistsPojoException {
        log.debug("Create Tag from {}", entity);
        final Optional<Tag> tmpt = tagRepository.findById(entity.getName());
        if (tmpt.isPresent()) {
            log.warn("Tag {} already exists.", tmpt.get());
            throw new AlreadyExistsPojoException(
                    "Tag already exists for name " + entity.getName());
        }
        final Tag saved = tagRepository.save(entity);
        log.debug("Saved entity: {}", saved);
        return saved;
    }

    /**
     * Update an existing tag.
     *
     * @param entity
     *            the Tag
     * @return TagDto of the updated entity.
     * @throws NotExistsPojoException
     *             If an Exception occurred
     */
    @Transactional
    public Tag updateTag(Tag entity) throws NotExistsPojoException {
        log.debug("Update tag from dto {}", entity
        );
        final Optional<Tag> tmpt = tagRepository.findById(entity.getName());
        if (!tmpt.isPresent()) {
            log.debug("Cannot update tag {} : resource does not exists.. ", entity);
            throw new NotExistsPojoException("Tag does not exists for name " + entity.getName());
        }
        final Tag toupd = tmpt.get();
        toupd.setDescription(entity.getDescription());
        toupd.setObjectType(entity.getObjectType());
        toupd.setSynchronization(entity.getSynchronization());
        toupd.setEndOfValidity(entity.getEndOfValidity());
        toupd.setLastValidatedTime(entity.getLastValidatedTime());
        toupd.setTimeType(entity.getTimeType());
        final Tag saved = tagRepository.save(toupd);
        log.debug("Updated entity: {}", saved);
        return saved;
    }

    /**
     * @param name
     *            the String
     */
    @Transactional
    public void removeTag(String name) {
        log.debug("Remove tag {}", name);
        tagRepository.deleteById(name);
        log.debug("Removed entity: {}", name);
    }

    /**
     * Insert new tag meta data.
     *
     * @param dto
     *            the TagMetaDto
     * @return TagMetaDto
     * @throws AlreadyExistsPojoException
     *             If an Exception occurred
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public TagMetaDto insertTagMeta(TagMetaDto dto)
            throws CdbServiceException, AlreadyExistsPojoException {
        log.debug("Create tag meta data from dto {}", dto);
        final TagMetaDto tmpt = tagmetaRepository.find(dto.getTagName());
        if (tmpt != null) {
            log.debug("Cannot store tag meta {} : resource already exists.. ", dto);
            throw new AlreadyExistsPojoException(
                    "Tag meta already exists for name " + dto.getTagName());
        }
        final TagMetaDto saved = tagmetaRepository.save(dto);
        log.info("Saved entity: {}", saved);
        return saved;
    }

    /**
     * Update an existing tag meta data.
     *
     * @param dto
     *            the TagMetaDto
     * @return TagMetaDto
     * @throws CdbServiceException If an exception occurred.
     */
    @Transactional
    public TagMetaDto updateTagMeta(TagMetaDto dto) throws CdbServiceException {
        log.debug("Update tag meta from dto {}", dto);
        final TagMetaDto saved = tagmetaRepository.update(dto);
        log.debug("Updated entity: {}", saved);
        return saved;
    }

    /**
     * @param id
     *            the String
     * @return TagMetaDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public TagMetaDto findMeta(String id) throws CdbServiceException {
        log.debug("Search for tag meta data by Id...{}", id);
        final TagMetaDto tmpt = tagmetaRepository.find(id);
        return tmpt; // This will trigger a response 404 if it is null
    }
    
    /**
     * Remote tag meta.
     *
     * @param name the name
     * @throws CdbServiceException the cdb service exception
     */
    public void removeTagMeta(String name) throws CdbServiceException {
        log.debug("Remove tag meta info for {}", name);
        tagmetaRepository.delete(name);
        log.debug("Removed tag meta info for: {}", name);  
    }
}
