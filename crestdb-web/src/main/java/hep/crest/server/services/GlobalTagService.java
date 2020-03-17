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
    private static final Logger log = LoggerFactory.getLogger(GlobalTagService.class);

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

    /**
     * @param globaltagname
     *            the String
     * @throws NotExistsPojoException
     *             If object was not found
     * @return GlobalTagDto
     */
    public GlobalTagDto findOne(String globaltagname) throws NotExistsPojoException {
        log.debug("Search for global tag by name {}", globaltagname);
        final GlobalTag entity = globalTagRepository.findByName(globaltagname);
        if (entity == null) {
            throw new NotExistsPojoException("Cannot find global tag " + globaltagname);
        }
        return mapper.map(entity, GlobalTagDto.class);
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
        GlobalTag entity = null;
        log.debug("Search for (record, label) specified tag list for GT: {}", globaltagname);
        if ("none".equals(record)) {
            record = "";
        }
        if ("none".equals(label)) {
            label = "";
        }
        if (record.isEmpty() && label.isEmpty()) {
            entity = globalTagRepository.findByNameAndFetchTagsEagerly(globaltagname);
        }
        else if (label.isEmpty()) {
            entity = globalTagRepository.findByNameAndFetchRecordTagsEagerly(globaltagname, record);
        }
        else {
            entity = globalTagRepository.findByNameAndFetchSpecifiedTagsEagerly(globaltagname,
                    record, label);
        }
        return getTagsFromGT(entity);
    }

    /**
     * @param dto
     *            the GlobalTagDto
     * @return GlobalTagDto
     * @throws CdbServiceException
     *             If an Exception occurred
     * @throws AlreadyExistsPojoException
     *             If an Exception occurred because pojo exists
     */
    @Transactional
    public GlobalTagDto insertGlobalTag(GlobalTagDto dto)
            throws CdbServiceException, AlreadyExistsPojoException {
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
            log.error("Cannot store global tag {} : resource already exists..{}", dto, e);
            throw e;
        }
    }

    /**
     * @param dto
     *            the GlobaTagDto
     * @return GlobalTagDto
     * @throws NotExistsPojoException
     *             If object was not found
     */
    @Transactional
    public GlobalTagDto updateGlobalTag(GlobalTagDto dto) throws NotExistsPojoException {
        log.debug("Create global tag from dto {}", dto);
        final GlobalTag entity = mapper.map(dto, GlobalTag.class);
        final Optional<GlobalTag> tmpoptgt = globalTagRepository.findById(entity.getName());
        if (!tmpoptgt.isPresent()) {
            log.debug("Cannot update global tag {} : resource does not exists.. ", dto);
            throw new NotExistsPojoException(
                    "Global tag does not exists for name " + dto.getName());
        }
        final GlobalTag toupd = tmpoptgt.get();
        toupd.setDescription(dto.getDescription());
        toupd.setRelease(dto.getRelease());
        toupd.setScenario(dto.getScenario());
        toupd.setSnapshotTime(dto.getSnapshotTime());
        final char type = dto.getType() != null ? dto.getType().charAt(0) : 'N';
        toupd.setType(type);
        toupd.setValidity(dto.getValidity());
        toupd.setWorkflow(dto.getWorkflow());
        final GlobalTag saved = globalTagRepository.save(toupd);
        log.debug("Saved entity: {}", saved);
        return mapper.map(saved, GlobalTagDto.class);
    }

    /**
     * @param name
     *            the String
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public void removeGlobalTag(String name) throws CdbServiceException {
        log.debug("Remove global tag {}", name);
        globalTagRepository.deleteById(name);
        log.debug("Removed entity: {}", name);
    }
}
