/**
 *
 */
package hep.crest.server.services;

import hep.crest.server.annotations.ProfileAndLog;
import hep.crest.server.caching.CachingProperties;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.repositories.IovGroupsCustom;
import hep.crest.server.data.repositories.IovRepository;
import hep.crest.server.data.repositories.PayloadRepository;
import hep.crest.server.data.repositories.TagRepository;
import hep.crest.server.data.repositories.args.IovQueryArgs;
import hep.crest.server.exceptions.AbstractCdbServiceException;
import hep.crest.server.exceptions.CdbNotFoundException;
import hep.crest.server.repositories.monitoring.IMonitoringRepository;
import hep.crest.server.swagger.model.CrestBaseResponse;
import hep.crest.server.swagger.model.IovDto;
import hep.crest.server.swagger.model.IovPayloadDto;
import hep.crest.server.swagger.model.IovSetDto;
import hep.crest.server.swagger.model.TagSummaryDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author formica
 */
@Service
@Slf4j
@Getter
public class IovService {

    /**
     * Repository.
     */
    private IovRepository iovRepository;
    /**
     * Repository.
     */
    private TagRepository tagRepository;
    /**
     * Repository.
     */
    private IMonitoringRepository iMonitoringRepository;

    /**
     * Repository.
     */
    private IovGroupsCustom iovGroupsCustom;

    /**
     * Repository.
     */
    private PayloadRepository payloadRepository;

    /**
     * Helper.
     */
    private PageRequestHelper prh;

    /**
     * Properties.
     */
    private CachingProperties cprops;

    /**
     * Ctor with injection.
     * @param iovRepository
     * @param tagRepository
     * @param iMonitoringRepository
     * @param iovGroupsCustom
     * @param payloadRepository
     * @param prh
     * @param cprops
     */
    @Autowired
    public IovService(IovRepository iovRepository, TagRepository tagRepository,
                      IMonitoringRepository iMonitoringRepository, IovGroupsCustom iovGroupsCustom,
                      PayloadRepository payloadRepository, PageRequestHelper prh,
                      CachingProperties cprops) {
        this.iovRepository = iovRepository;
        this.tagRepository = tagRepository;
        this.iMonitoringRepository = iMonitoringRepository;
        this.iovGroupsCustom = iovGroupsCustom;
        this.payloadRepository = payloadRepository;
        this.prh = prh;
        this.cprops = cprops;
    }

    /**
     * @param tagname   the String
     * @param snapshot  the Date
     * @param groupsize the Long
     * @return List<BigInteger>
     */
    public List<BigInteger> selectGroupsByTagNameAndSnapshotTime(String tagname, Date snapshot,
                                                                 Long groupsize) {
        log.debug("Search for iovs groups by tag name {} and snapshot time {}", tagname, snapshot);
        List<BigInteger> minsincelist = iovGroupsCustom.selectSnapshotGroups(tagname, snapshot,
                groupsize);
        if (minsincelist == null) {
            minsincelist = new ArrayList<>();
        }
        return minsincelist;
    }

    /**
     * @param tagname   the String
     * @param snapshot  the Date
     * @param groupsize the Long
     * @return CrestBaseResponse
     */
    @ProfileAndLog
    public CrestBaseResponse selectGroupDtoByTagNameAndSnapshotTime(String tagname, Date snapshot,
                                                                    Long groupsize) {
        final List<BigInteger> minsincelist = selectGroupsByTagNameAndSnapshotTime(tagname,
                snapshot, groupsize);
        final List<IovDto> iovlist =
                minsincelist.stream().map(s -> new IovDto().since(s.longValue()).tagName(tagname))
                        .collect(Collectors.toList());
        return new IovSetDto().resources(iovlist).size((long) iovlist.size());
    }

    /**
     * Select Iovs.
     *
     * @param args
     * @param preq
     * @return Page of Iov
     */
    public Page<Iov> selectIovList(IovQueryArgs args, Pageable preq) {
        Page<Iov> entitylist = null;
        if (preq == null) {
            String sort = "id.since:ASC,id.insertionTime:DESC";
            preq = prh.createPageRequest(0, 1000, sort);
        }
        entitylist = iovRepository.findIovList(args, preq);
        log.trace("Retrieved list of iovs {}", entitylist);
        return entitylist;
    }

    /**
     * @param tagname  the String
     * @param since    the BigInteger
     * @param until    the BigInteger
     * @param snapshot the Date
     * @return List<IovPayloadDto>
     */
    public List<IovPayloadDto> selectIovPayloadsByTagRangeSnapshot(String tagname, BigInteger since,
                                                                   BigInteger until,
                                                                   Date snapshot) {
        log.debug("Search for iovs by tag name {}  and range time {} -> {} using snapshot {}",
                tagname, since, until, snapshot);
        List<IovPayloadDto> entities = null;
        if (snapshot == null || snapshot.getTime() == 0) {
            snapshot = Date.from(Instant.now()); // Use now for the snapshot
        }
        entities = iMonitoringRepository.getRangeIovPayloadInfo(tagname, since, until, snapshot);

        if (entities == null) {
            log.warn("Cannot find iovpayloads for tag {} using ranges {} {} and snapshot {}",
                    tagname, since, until, snapshot);
            return new ArrayList<>();
        }
        return entities;
    }

    /**
     * @param tagname the String
     * @return Long
     */
    public Long getSizeByTag(String tagname) {
        log.debug("Count number of iovs by tag name {}", tagname);
        return iovGroupsCustom.getSize(tagname);
    }

    /**
     * @param tagname  the String
     * @param snapshot the Date
     * @return Long
     */
    public Long getSizeByTagAndSnapshot(String tagname, Date snapshot) {
        log.debug("Count number of iovs by tag name {} and snapshot {}", tagname, snapshot);
        return iovGroupsCustom.getSizeBySnapshot(tagname, snapshot);
    }

    /**
     * @param tagname the String
     * @return List<TagSummaryDto>
     */
    public List<TagSummaryDto> getTagSummaryInfo(String tagname) {
        log.debug("Tag summary by tag name {}", tagname);
        List<TagSummaryDto> entitylist = iMonitoringRepository.getTagSummaryInfo(tagname);
        if (entitylist == null) {
            entitylist = new ArrayList<>();
        }
        return entitylist;
    }

    /**
     * Return the last iov of a tag.
     *
     * @param tagname
     * @return Iov
     */
    public Iov latest(String tagname) {
        String sort = "id.since:DESC,id.insertionTime:DESC";
        Pageable preq = prh.createPageRequest(0, 1, sort);
        Page<Iov> lastiovlist = iovRepository.findByIdTagName(tagname, preq);
        if (lastiovlist == null || lastiovlist.getSize() == 0) {
            throw new CdbNotFoundException("Cannot find last iov for tag " + tagname);
        }
        return lastiovlist.toList().get(0);
    }

    /**
     * @param tagname the String
     * @param since   the BigInteger
     * @param hash    the String
     * @return Iov or null
     */
    public Iov existsIov(String tagname, BigInteger since, String hash) {
        log.debug("Verify if the same IOV is already stored with the same hash....");
        final Iov tmpiov = iovRepository.exists(tagname, since, hash);
        return tmpiov;
    }

    /**
     * @param entity the IovDto
     * @return Iov
     * @throws AbstractCdbServiceException     If an Exception occurred
     * @throws DataIntegrityViolationException If an sql exception occurred.
     */
    @Transactional(rollbackOn = {AbstractCdbServiceException.class})
    public Iov insertIov(Iov entity) throws AbstractCdbServiceException {
        log.debug("Create iov only [iov={}]", entity);
        final String tagname = entity.getTag().getName();
        final Tag t = checkTag(tagname);
        // Check if payload exists. Cannot store IOV without payload.
        if (!entity.getPayloadHash().startsWith("triggerdb")
                & payloadRepository.findById(entity.getPayloadHash()).isEmpty()) {
            log.warn("Payload not found for hash: {}", entity.getPayloadHash());
            throw new CdbNotFoundException("Payload not found: " + entity.getPayloadHash());
        }
        // Save iov in the given tag.
        log.debug("Storing iov entity {} in tag {}", entity, t);
        entity.setTag(t);
        final Iov saved = iovRepository.save(entity);
        log.trace("Saved iov entity: {}", saved);
        return saved;
    }

    /**
     * Set the group size for a tag.
     * The group size is used to compute the iov groups. We can have different group sizes,
     * the optimal data should be taken from the DB.
     *
     * @param timetype
     * @return Long the group size
     */
    public Long getOptimalGroupSize(String timetype) {
        Long groupsize;
        if (timetype.equalsIgnoreCase("run")) {
            // The iov is of type RUN. Use the group size from properties.
            groupsize = Long.valueOf(cprops.getRuntypeGroupsize());
        }
        else if (timetype.equalsIgnoreCase("run-lumi")) {
            // The iov is of type RUN-LUMI. Use the group size from properties.
            groupsize = Long.valueOf(cprops.getRuntypeGroupsize());
        }
        else if (timetype.equalsIgnoreCase("sec")) {
            // The iov is of type sec. Use the group size from properties.
            // It should correspond to the number of seconds in a day by default.
            groupsize = Long.valueOf(cprops.getTimetypeGroupsize());
        }
        else {
            // Assume COOL time format...
            groupsize = Long.valueOf(cprops.getRuntypeGroupsize());
        }
        return groupsize;
    }

    /**
     * Update the insertion time of the IOV.
     * @param entity
     */
    public void updateIov(Iov entity) {
        int nupd = iovRepository.updateIov(entity.getTag().getName(), entity.getId().getSince(),
                entity.getPayloadHash(), entity.getId().getInsertionTime());
        log.debug("Updated iov entity: {} with result {}", entity, nupd);
    }

    /**
     * Non-transactional iov storage.
     * Used to avoid updating tag as done in IovService.
     * This method is called internally by the transactional method saveAll.
     * You cannot intercept this with Aspect.
     *
     * @param entity
     * @return Iov
     */
    public Iov storeIov(Iov entity) {
        log.debug("Create iov + payload [iov= {}]", entity);
        final String tagname = entity.getTag().getName();
        // The IOV is not yet stored. Verify that the tag exists before inserting it.
        Tag t = checkTag(tagname);
        entity.setTag(t);
        entity.getId().setTagName(t.getName());
        log.debug("Storing iov entity {} in tag {}", entity, t);
        final Iov saved = iovRepository.save(entity);
        log.trace("Saved iov entity: {}", saved);
        return saved;
    }

    /**
     * Check the tag before inserting the iov.
     *
     * @param tagname
     * @return Tag
     */
    protected Tag checkTag(String tagname) {
        // The IOV is not yet stored. Verify that the tag exists before inserting it.
        final Optional<Tag> tg = tagRepository.findById(tagname);
        if (tg.isEmpty()) {
            throw new CdbNotFoundException("Tag " + tagname + " not found: cannot insert IOV.");
        }
        return tg.get();
    }

    /**
     * Remove a list of iovs, send back the hash of payloads.
     *
     * @param iovList the List of Iov
     * @return List<String>
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected List<String> removeIovList(List<Iov> iovList) {
        List<String> hashList = new ArrayList<>();
        int i = 0;
        for (Iov iov : iovList) {
            i++;
            if ((i % 100) == 0) {
                log.debug("Delete iov {}....[{}]", iov, i);
            }
            hashList.add(iov.getPayloadHash());
            iovRepository.delete(iov);
        }
        iovRepository.flush();
        return hashList;
    }

}
