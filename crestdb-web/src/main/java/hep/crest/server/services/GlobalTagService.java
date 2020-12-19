/**
 *
 */
package hep.crest.server.services;

import com.querydsl.core.types.Predicate;
import hep.crest.data.pojo.GlobalTag;
import hep.crest.data.pojo.GlobalTagMap;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.GlobalTagRepository;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.server.exceptions.NotExistsPojoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
     * @param qry
     *            the Predicate
     * @param req
     *            the Pageable
     * @return List<GlobalTag>
     */
    public Iterable<GlobalTag> findAllGlobalTags(Predicate qry, Pageable req) {
        Iterable<GlobalTag> entitylist = null;
        if (qry == null) {
            if (req == null) {
                entitylist = globalTagRepository.findAll();
            }
            else {
                entitylist = globalTagRepository.findAll(req);
            }
        }
        else {
            entitylist = globalTagRepository.findAll(qry, req);
        }
        return entitylist;
    }

    /**
     * @param globaltagname
     *            the String
     * @throws NotExistsPojoException
     *             If object was not found
     * @return GlobalTag
     */
    public GlobalTag findOne(String globaltagname) {
        log.debug("Search for global tag by name {}", globaltagname);
        final GlobalTag entity = globalTagRepository.findByName(globaltagname);
        if (entity == null) {
            throw new NotExistsPojoException("Cannot find global tag " + globaltagname);
        }
        return entity;
    }

    /**
     * @param globaltagname
     *            the String
     * @param record
     *            the String
     * @param label
     *            the String
     * @return List<Tag>
     * @throws NotExistsPojoException
     *             If an Exception occurred
     */
    public List<Tag> getGlobalTagByNameFetchTags(String globaltagname, String record, String label) {
        GlobalTag entity = null;
        log.debug("Search for (record, label) specified tag list for GlobalTag={}", globaltagname);
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
        if (entity == null) {
            throw new NotExistsPojoException("Cannot fetch tags for " + globaltagname);
        }
        final List<Tag> taglist = new ArrayList<>();
        for (final GlobalTagMap globalTagMap : entity.getGlobalTagMaps()) {
            taglist.add(globalTagMap.getTag());
        }
        return taglist;
    }

    /**
     * @param entity
     *            the GlobalTag
     * @return GlobalTag
     * @throws AlreadyExistsPojoException
     *             If an Exception occurred because pojo exists
     */
    @Transactional
    public GlobalTag insertGlobalTag(GlobalTag entity) {
        log.debug("Create GlobalTag from {}", entity);
        final Optional<GlobalTag> tmpgt = globalTagRepository.findById(entity.getName());
        if (tmpgt.isPresent()) {
            log.warn("GlobalTag {} already exists.", tmpgt.get());
            throw new AlreadyExistsPojoException(
                    "GlobalTag already exists for name " + entity.getName());
        }
        final GlobalTag saved = globalTagRepository.save(entity);
        log.debug("Saved entity {}", saved);
        return saved;
    }

    /**
     * @param entity
     *            the GlobaTag
     * @return GlobalTag
     * @throws NotExistsPojoException
     *             If object was not found
     */
    @Transactional
    public GlobalTag updateGlobalTag(GlobalTag entity) {
        log.debug("Update GlobalTag from {}", entity);
        final Optional<GlobalTag> tmpoptgt = globalTagRepository.findById(entity.getName());
        if (!tmpoptgt.isPresent()) {
            log.debug("Cannot update GlobalTag {} : resource does not exists.. ", entity.getName());
            throw new NotExistsPojoException(
                    "GlobalTag does not exists for name " + entity.getName());
        }
        final GlobalTag toupd = tmpoptgt.get();
        toupd.setDescription(entity.getDescription());
        toupd.setRelease(entity.getRelease());
        toupd.setScenario(entity.getScenario());
        toupd.setSnapshotTime(entity.getSnapshotTime());
        toupd.setType(entity.getType());
        toupd.setValidity(entity.getValidity());
        toupd.setWorkflow(entity.getWorkflow());
        final GlobalTag saved = globalTagRepository.save(toupd);
        log.debug("Saved entity: {}", saved);
        return saved;
    }

    /**
     * @param name
     *            the String
     */
    @Transactional
    public void removeGlobalTag(String name) {
        log.debug("Remove global tag {}", name);
        globalTagRepository.deleteById(name);
        log.debug("Removed entity: {}", name);
    }
}
