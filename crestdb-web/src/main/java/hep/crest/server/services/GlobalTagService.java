/**
 *
 */
package hep.crest.server.services;

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

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Repository.
     */
    @Autowired
    private GlobalTagRepository globalTagRepository;

    /**
     * Mapper.
     */
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
     * @param qry
     *            the Predicate
     * @param req
     *            the Pageable
     * @return List<GlobalTagDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<GlobalTagDto> findAllGlobalTags(Predicate qry, Pageable req)
            throws CdbServiceException {
        try {
            Iterable<GlobalTag> entitylist = null;
            if (qry == null) {
                entitylist = globalTagRepository.findAll(req);
            }
            else {
                entitylist = globalTagRepository.findAll(qry, req);
            }
            return StreamSupport.stream(entitylist.spliterator(), false)
                    .map(s -> mapper.map(s, GlobalTagDto.class)).collect(Collectors.toList());
        }
        catch (final Exception e) {
            throw new CdbServiceException("Cannot find global tag list " + e.getMessage());
        }
    }

    /**
     * @param globaltagname
     *            the String
     * @return GlobalTagDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public GlobalTagDto findOne(String globaltagname) throws CdbServiceException {
        try {
            log.debug("Search for global tag by name {}", globaltagname);
            final GlobalTag entity = globalTagRepository.findByName(globaltagname);
            return mapper.map(entity, GlobalTagDto.class);
        }
        catch (final Exception e) {
            log.debug("Exception in retrieving global tag list using ByName expression...{}",
                    globaltagname);
            throw new CdbServiceException("Cannot find global tag by name: " + e.getMessage());
        }
    }

    /**
     * @param gt
     *            the GlobalTag
     * @return List<TagDto>
     * @throws EmptyPojoException
     *             If an Exception occurred
     */
    private List<TagDto> getTagsFromGT(GlobalTag gt) throws EmptyPojoException {
        if (gt == null) {
            throw new EmptyPojoException("Cannot load tags from a null global tag...");
        }
        final Set<GlobalTagMap> gtMaps = gt.getGlobalTagMaps();
        final List<TagDto> tagDtos = new ArrayList<>();
        for (final GlobalTagMap globalTagMap : gtMaps) {
            tagDtos.add(mapper.map(globalTagMap.getTag(), TagDto.class));
        }
        return tagDtos;
    }

    /**
     * @param globaltagname
     *            the String
     * @param record
     *            the String
     * @param label
     *            the String
     * @return List<TagDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<TagDto> getGlobalTagByNameFetchTags(String globaltagname, String record,
            String label) throws CdbServiceException {
        try {
            GlobalTag entity = null;
            log.debug("Search for (record, label) specified tag list for GT: {}", globaltagname);
            if (record.equals("") && label.equals("")) {
                entity = globalTagRepository.findByNameAndFetchTagsEagerly(globaltagname);
            }
            else if (label.equals("")) {
                entity = globalTagRepository.findByNameAndFetchRecordTagsEagerly(globaltagname,
                        record);
            }
            else {
                entity = globalTagRepository.findByNameAndFetchSpecifiedTagsEagerly(globaltagname,
                        record, label);
            }

            return getTagsFromGT(entity);
        }
        catch (final Exception e) {
            log.debug("Exception in retrieving specified tag list with record and label for GT: {}",
                    globaltagname);
            throw new CdbServiceException(
                    "Cannot find specified tag list for GT: " + e.getMessage());
        }

    }

    /**
     * @param dto
     *            the GlobalTagDto
     * @return GlobalTagDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public GlobalTagDto insertGlobalTag(GlobalTagDto dto) throws CdbServiceException {
        try {
            log.debug("Create global tag from dto {}", dto);
            final GlobalTag entity = mapper.map(dto, GlobalTag.class);
            final Optional<GlobalTag> tmpgt = globalTagRepository.findById(entity.getName());
            if (tmpgt.isPresent()) {
                log.error("global tag {} already exists..", tmpgt.get());
                throw new AlreadyExistsPojoException(
                        "Global tag already exists for name " + dto.getName());
            }
            log.debug("Saving global tag entity {}", entity);
            final GlobalTag saved = globalTagRepository.save(entity);
            log.debug("Saved entity: {}", saved);
            return mapper.map(saved, GlobalTagDto.class);
        }
        catch (final AlreadyExistsPojoException e) {
            log.error("Cannot store global tag {} : resource already exists.. ", dto);
            throw e;
        }
        catch (final ConstraintViolationException e) {
            log.error("Cannot store global tag {} : may be the resource already exists..", dto);
            throw new AlreadyExistsPojoException("Global tag already exists : " + e.getMessage());
        }
        catch (final Exception e) {
            log.error("Cannot store global tag {} : resource already exists.. ", dto);
            throw new CdbServiceException("Cannot store global tag : " + e.getMessage());
        }
    }

    /**
     * @param dto
     *            the GlobaTagDto
     * @return GlobalTagDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public GlobalTagDto updateGlobalTag(GlobalTagDto dto) throws CdbServiceException {
        try {
            log.debug("Create global tag from dto {}", dto);
            final GlobalTag entity = mapper.map(dto, GlobalTag.class);
            final GlobalTag tmpgt = globalTagRepository.findByName(entity.getName());
            if (tmpgt == null) {
                log.debug("Cannot update global tag {} : resource does not exists.. ", dto);
                throw new NotExistsPojoException(
                        "Global tag does not exists for name " + dto.getName());
            }
            final GlobalTag saved = globalTagRepository.save(entity);
            log.debug("Saved entity: {}", saved);
            return mapper.map(saved, GlobalTagDto.class);
        }
        catch (final NotExistsPojoException e) {
            log.error("Cannot store global tag {} : resource does not exists.. ", dto);
            throw e;
        }
        catch (final ConstraintViolationException e) {
            log.error("Cannot store global tag {} : resource does not exists ? ", dto);
            throw new NotExistsPojoException("Global tag does not exists : " + e.getMessage());
        }
        catch (final Exception e) {
            log.error("Exception in storing global tag {}", dto);
            throw new CdbServiceException("Cannot store global tag : " + e.getMessage());
        }
    }

    /**
     * @param name
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public void removeGlobalTag(String name) throws CdbServiceException {
        try {
            log.debug("Remove global tag {}", name);
            globalTagRepository.deleteById(name);
            log.debug("Removed entity: {}", name);
        }
        catch (final Exception e) {
            log.error("Exception in removing global tag {}", name);
            throw new CdbServiceException("Cannot remove global tag : " + e.getMessage());
        }
    }
}
