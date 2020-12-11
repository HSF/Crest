/**
 *
 */
package hep.crest.server.services;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.IovGroupsCustom;
import hep.crest.data.repositories.IovRepository;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.data.repositories.TagRepository;
import hep.crest.data.repositories.querydsl.IFilteringCriteria;
import hep.crest.server.annotations.ProfileAndLog;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.AlreadyExistsIovException;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovPayloadDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.TagSummaryDto;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author formica
 *
 */
@Service
public class IovService {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IovService.class);

    /**
     * Repository.
     */
    @Autowired
    private IovRepository iovRepository;
    /**
     * Repository.
     */
    @Autowired
    private TagRepository tagRepository;
    /**
     * Repository.
     */
    @Autowired
    @Qualifier("payloaddatadbrepo")
    private PayloadDataBaseCustom payloaddataRepository;

    /**
     * Repository.
     */
    @Autowired
    @Qualifier("iovgroupsrepo")
    private IovGroupsCustom iovgroupsrepo;
    /**
     * Helper.
     */
    @Autowired
    private PageRequestHelper prh;

    /**
     * Filtering.
     */
    @Autowired
    @Qualifier("iovFiltering")
    private IFilteringCriteria filtering;

    /**
     * @param tagname
     *            the String
     * @param since
     *            the String
     * @param dateformat
     *            the String
     * @return IovDto
     */
    public Iov latest(String tagname, String since, String dateformat) {

        final PageRequest preq = prh.createPageRequest(0, 10, "id.since:DESC");
        if ("now".equals(since)) {
            since = ((Long) Instant.now().getMillis()).toString();
        }
        final String by = "tagname:" + tagname + ",since<" + since;
        final BooleanExpression wherepred = prh.buildWhere(filtering, by);

        final Iterable<Iov> entitylist = this.findAllIovs(wherepred, preq);
        if (entitylist != null && entitylist.iterator().hasNext()) {
            return entitylist.iterator().next();
        }
        return null;
    }

    /**
     * @param qry
     *            the Predicate
     * @param req
     *            the Pageable
     * @return Iterable<Iov>
     */
    public Iterable<Iov> findAllIovs(Predicate qry, Pageable req) {
        if (req == null) {
            throw new IllegalArgumentException("Pagination parameter are mandatory for IOVs");
        }
        Iterable<Iov> entitylist = null;
        if (qry == null) {
            entitylist = iovRepository.findAll(req);
        }
        else {
            entitylist = iovRepository.findAll(qry, req);
        }
        return entitylist;
    }

    /**
     * @param tagname
     *            the String
     * @param snapshot
     *            the Date
     * @param groupsize
     *            the Long
     * @return List<BigDecimal>
     */
    public List<BigDecimal> selectGroupsByTagNameAndSnapshotTime(String tagname, Date snapshot,
                                                                 Long groupsize) {
        log.debug("Search for iovs groups by tag name {} and snapshot time {}", tagname, snapshot);
        List<BigDecimal> minsincelist = null;
        if (snapshot == null) {
            minsincelist = iovgroupsrepo.selectGroups(tagname, groupsize);
        }
        else {
            minsincelist = iovgroupsrepo.selectSnapshotGroups(tagname, snapshot, groupsize);
        }
        if (minsincelist == null) {
            minsincelist = new ArrayList<>();
        }
        return minsincelist;
    }

    /**
     * @param tagname
     *            the String
     * @param snapshot
     *            the Date
     * @param groupsize
     *            the Long
     * @return CrestBaseResponse
     */
    @ProfileAndLog
    public CrestBaseResponse selectGroupDtoByTagNameAndSnapshotTime(String tagname, Date snapshot,
                                                                    Long groupsize) {
        final List<BigDecimal> minsincelist = selectGroupsByTagNameAndSnapshotTime(tagname,
                snapshot, groupsize);
        final List<IovDto> iovlist = minsincelist.stream().map(s -> new IovDto().since(s))
                .collect(Collectors.toList());
        return new IovSetDto().resources(iovlist).size((long) iovlist.size()).format("IovSetDto");
    }

    /**
     * @param tagname
     *            the String
     * @param since
     *            the BigDecimal
     * @param until
     *            the BigDecimal
     * @param snapshot
     *            the Date
     * @param flag
     *            the String
     * @return Iterable<Iov>
     */
    public Iterable<Iov> selectIovsByTagRangeSnapshot(String tagname, BigDecimal since,
                                                      BigDecimal until, Date snapshot, String flag) {
        log.debug("Search for iovs by tag name {}  and range time {} -> {} using snapshot {} and flag {}",
                tagname, since, until, snapshot, flag);
        Iterable<Iov> entities = null;
        if (snapshot == null && "groups".equals(flag)) {
            entities = iovRepository.selectLatestByGroup(tagname, since, until);
        }
        else if ("groups".equals(flag)) {
            entities = iovRepository.selectSnapshotByGroup(tagname, since, until, snapshot);
        }
        else if ("ranges".equals(flag)) {
            if (snapshot == null) {
                snapshot = Instant.now().toDate();
            }
            entities = iovRepository.getRange(tagname, since, until, snapshot);
        }
        else {
            log.warn("Unkown header flag {}, sending empty Iov list.", flag);
        }
        if (entities == null) {
            log.warn("Cannot find iovs for tag {}", tagname);
            return new ArrayList<>();
        }
        return entities;
    }

    /**
     * @param tagname
     *            the String
     * @param since
     *            the BigDecimal
     * @param until
     *            the BigDecimal
     * @param snapshot
     *            the Date
     * @return List<IovPayloadDto>
     */
    public List<IovPayloadDto> selectIovPayloadsByTagRangeSnapshot(String tagname, BigDecimal since,
                                                                   BigDecimal until, Date snapshot) {
        log.debug("Search for iovs by tag name {}  and range time {} -> {} using snapshot {}",
                tagname, since, until, snapshot);
        List<IovPayloadDto> entities = null;
        if (snapshot == null || snapshot.getTime() == 0) {
            snapshot = Instant.now().toDate(); // Use now for the snapshot
        }
        entities = iovgroupsrepo.getRangeIovPayloadInfo(tagname, since, until, snapshot);

        if (entities == null) {
            log.warn("Cannot find iovpayloads for tag {} using ranges {} {} and snapshot {}",
                    tagname, since, until, snapshot);
            return new ArrayList<>();
        }
        return entities;
    }

    /**
     * @param tagname
     *            the String
     * @param snapshot
     *            the Date
     * @return Iterable<Iov>
     */
    public Iterable<Iov> selectSnapshotByTag(String tagname, Date snapshot) {
        log.debug("Search for snapshot by tag name {} using snapshot {}", tagname, snapshot);
        return iovRepository.selectSnapshot(tagname, snapshot);
    }

    /**
     * @param tagname
     *            the String
     * @return Long
     */
    public Long getSizeByTag(String tagname) {
        log.debug("Count number of iovs by tag name {}", tagname);
        return iovgroupsrepo.getSize(tagname);
    }

    /**
     * @param tagname
     *            the String
     * @param snapshot
     *            the Date
     * @return Long
     */
    public Long getSizeByTagAndSnapshot(String tagname, Date snapshot) {
        log.debug("Count number of iovs by tag name {} and snapshot {}", tagname, snapshot);
        return iovgroupsrepo.getSizeBySnapshot(tagname, snapshot);
    }

    /**
     * @param tagname
     *            the String
     * @return List<TagSummaryDto>
     */
    public List<TagSummaryDto> getTagSummaryInfo(String tagname) {
        log.debug("Tag summary by tag name {}", tagname);
        List<TagSummaryDto> entitylist = iovgroupsrepo.getTagSummaryInfo(tagname);
        if (entitylist == null) {
            entitylist = new ArrayList<>();
        }
        return entitylist;
    }

    /**
     * @param tagname
     *            the String
     * @param since
     *            the BigDecimal
     * @param hash
     *            the String
     * @return boolean
     */
    public boolean existsIov(String tagname, BigDecimal since, String hash) {
        log.debug("Verify if the same IOV is already stored with the same hash....");
        final Iov tmpiov = iovRepository.findBySinceAndTagNameAndHash(tagname, since, hash);
        return tmpiov != null;
    }

    /**
     * @param entity
     *            the IovDto
     * @return Iov
     * @throws NotExistsPojoException
     *             If an Exception occurred
     * @throws DataIntegrityViolationException If an sql exception occurred.
     */
    @Transactional(rollbackOn = {CdbServiceException.class})
    public Iov insertIov(Iov entity) throws CdbServiceException, DataIntegrityViolationException {
        log.debug("Create iov from {}", entity);
        final String tagname = entity.getTag().getName();
        // The IOV is not yet stored. Verify that the tag exists before inserting it.
        final Optional<Tag> tg = tagRepository.findById(tagname);
        if (tg.isPresent()) {
            final Tag t = tg.get();
            t.setModificationTime(new Date());
            // Check if iov exists
            if (existsIov(t.getName(), entity.getId().getSince(), entity.getPayloadHash())) {
                log.warn("Iov already exists : {}", entity);
                throw new AlreadyExistsIovException(entity.toString());
            }
            // Update the tag modification time
            final Tag updtag = tagRepository.save(t);
            entity.setTag(updtag);
            entity.getId().setTagName(updtag.getName());
            log.debug("Storing iov entity {} in tag {}", entity, updtag);
            final Iov saved = iovRepository.save(entity);
            log.debug("Saved entity: {}", saved);
            return saved;
        }
        throw new NotExistsPojoException("Unkown tag : " + tagname);
    }
}
