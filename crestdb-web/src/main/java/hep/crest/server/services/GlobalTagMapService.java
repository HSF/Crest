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
import org.springframework.stereotype.Service;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.pojo.GlobalTagMapId;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.GlobalTagMapRepository;
import hep.crest.data.repositories.GlobalTagRepository;
import hep.crest.data.repositories.TagRepository;
import hep.crest.swagger.model.GlobalTagMapDto;
import ma.glasnost.orika.MapperFacade;

/**
 * @author formica
 * @author rsipos
 *
 */
@Service
public class GlobalTagMapService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalTagMapService.class);

    /**
     * Repository.
     */
    @Autowired
    private GlobalTagMapRepository globalTagMapRepository;
    /**
     * Repository.
     */
    @Autowired
    private GlobalTagRepository globalTagRepository;
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
     * @return the globalTagMapRepository
     */
    public GlobalTagMapRepository getGlobalTagMapRepository() {
        return globalTagMapRepository;
    }

    /**
     * @param gtName
     *            the String
     * @return List<GlobalTagMapDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<GlobalTagMapDto> getTagMap(String gtName) throws CdbServiceException {

        log.debug("Search for GlobalTagMap entries by GlobalTag name {}", gtName);
        final Iterable<GlobalTagMap> entitylist = globalTagMapRepository
                .findByGlobalTagName(gtName);
        return StreamSupport.stream(entitylist.spliterator(), false)
                .map(s -> mapper.map(s, GlobalTagMapDto.class)).collect(Collectors.toList());

    }

    /**
     * @param tagName
     *            the String
     * @return List<GlobalTagMapDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<GlobalTagMapDto> getTagMapByTagName(String tagName) throws CdbServiceException {

        log.debug("Search for GlobalTagMap entries by TAG name {}", tagName);

        final Iterable<GlobalTagMap> entitylist = globalTagMapRepository.findByTagName(tagName);
        return StreamSupport.stream(entitylist.spliterator(), false)
                .map(s -> mapper.map(s, GlobalTagMapDto.class)).collect(Collectors.toList());

    }

    /**
     * @param dto
     *            the GlobalTagMapDto
     * @return GlobalTagMapDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public GlobalTagMapDto insertGlobalTagMap(GlobalTagMapDto dto) throws CdbServiceException {
        log.debug("Create global tag map from dto {}", dto);
        final GlobalTagMap entity = new GlobalTagMap();
        final Optional<GlobalTag> gt = globalTagRepository.findById(dto.getGlobalTagName());
        final Optional<Tag> tg = tagRepository.findById(dto.getTagName());

        final GlobalTagMapId id = new GlobalTagMapId(dto.getGlobalTagName(), dto.getRecord(),
                dto.getLabel());
        entity.setId(id);
        gt.ifPresent(mgt -> {
            globalTagRepository.save(mgt);
            entity.setGlobalTag(mgt);
        });
        tg.ifPresent(mt -> 
            entity.setTag(mt)
        );
        final GlobalTagMap saved = globalTagMapRepository.save(entity);
        log.debug("Saved entity: {}", saved);
        return mapper.map(saved, GlobalTagMapDto.class);
    }
}
