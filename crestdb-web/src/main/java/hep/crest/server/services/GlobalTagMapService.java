/**
 *
 */
package hep.crest.server.services;

import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.GlobalTagMapRepository;
import hep.crest.data.repositories.GlobalTagRepository;
import hep.crest.data.repositories.TagRepository;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

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
     * @param gtName
     *            the String represnting the GlobalTag name
     * @return Iterable<GlobalTagMap>
     */
    public Iterable<GlobalTagMap> getTagMap(String gtName) {
        log.debug("Search for GlobalTagMap entries by GlobalTag name {}", gtName);
        return globalTagMapRepository
                .findByGlobalTagName(gtName);
    }

    /**
     * @param tagName
     *            the String
     * @return Iterable<GlobalTagMapDto>
     */
    public Iterable<GlobalTagMap> getTagMapByTagName(String tagName) {
        log.debug("Search for GlobalTagMap entries by Tag name {}", tagName);
        return globalTagMapRepository
                .findByTagName(tagName);
    }

    /**
     * @param entity
     *            the GlobalTagMap
     * @return GlobalTagMap
     * @throws NotExistsPojoException
     *             If an Exception occurred
     * @throws AlreadyExistsPojoException
     *             If an Exception occurred
     */
    @Transactional
    public GlobalTagMap insertGlobalTagMap(GlobalTagMap entity)
            throws NotExistsPojoException, AlreadyExistsPojoException {
        log.debug("Create GlobalTagMap from {}", entity);
        Optional<GlobalTagMap> map = globalTagMapRepository.findById(entity.getId());
        if (map.isPresent()) {
            log.warn("GlobalTagMap {} already exists.", map.get());
            throw new AlreadyExistsPojoException(
                    "GlobalTagMap already exists for ID " + entity.getId());
        }
        String gtname = entity.getId().getGlobalTagName();
        final Optional<GlobalTag> gt = globalTagRepository.findById(gtname);
        if (!gt.isPresent()) {
            log.warn("GlobalTag {} does not exists.", gtname);
            throw new NotExistsPojoException("GlobalTag does not exists for name " + gtname);
        }
        String tagname = entity.getTag().getName();
        final Optional<Tag> tg = tagRepository.findById(tagname);
        if (!tg.isPresent()) {
            log.warn("Tag {} does not exists.", tagname);
            throw new NotExistsPojoException("Tag does not exists for name " + tagname);
        }
        entity.setGlobalTag(gt.get());
        entity.setTag(tg.get());
        final GlobalTagMap saved = globalTagMapRepository.save(entity);
        log.debug("Saved entity: {}", saved);
        return saved;
    }
}
