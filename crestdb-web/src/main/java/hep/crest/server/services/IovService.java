/**
 *
 */
package hep.crest.server.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.data.repositories.IovGroupsCustom;
import hep.crest.data.repositories.IovRepository;
import hep.crest.data.repositories.PayloadDataBaseCustom;
import hep.crest.data.repositories.TagRepository;
import hep.crest.server.annotations.ProfileAndLog;
import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.exceptions.AlreadyExistsPojoException;
import hep.crest.swagger.model.CrestBaseResponse;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.IovSetDto;
import hep.crest.swagger.model.TagSummaryDto;
import ma.glasnost.orika.MapperFacade;

/**
 * @author formica
 *
 */
@Service
public class IovService {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
    private IovGroupsCustom iovgroupsrepo;

    /**
     * Mapper.
     */
    @Autowired
    @Qualifier("mapper")
    private MapperFacade mapper;

    /**
     * @param tagname
     *            the String
     * @return List<IovDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<IovDto> findAllIovsByTagName(String tagname) throws CdbServiceException {
        try {
            log.debug("Search for iovs by tag name {}", tagname);
            final List<Iov> entitylist = iovRepository.findByIdTagName(tagname);
            return entitylist.stream().map(s -> mapper.map(s, IovDto.class))
                    .collect(Collectors.toList());
        }
        catch (final Exception e) {
            log.error("Exception in retrieving iov list using IdByTagName expression...{}",
                    tagname);
            throw new CdbServiceException("Cannot find iovs by tag name: " + e.getMessage());
        }
    }

    /**
     * @param tagname
     *            the String
     * @return IovDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public IovDto latest(String tagname) throws CdbServiceException {
        try {
            log.debug("Search for latest iovs by tag name {} using pagination query", tagname);
            final PageRequestHelper prh = new PageRequestHelper();
            final PageRequest req = prh.createPageRequest(0, 1, "stime:DESC");
            final List<IovDto> iovlist = this.findAllIovsByTagName(tagname, req);
            if (iovlist.isEmpty()) {
                return null;
            }
            return iovlist.get(0);
        }
        catch (final Exception e) {
            log.debug(
                    "Exception in retrieving iov list using IdByTagName expression and pagination...{}",
                    tagname);
            throw new CdbServiceException(
                    "Cannot find iovs by tag name and pagination: " + e.getMessage());
        }
    }

    /**
     * @param tagname
     *            the String
     * @param req
     *            the Pageable
     * @return List<IovDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<IovDto> findAllIovsByTagName(String tagname, Pageable req)
            throws CdbServiceException {
        try {
            log.debug("Search for iovs by tag name {} using pagination request", tagname);
            final List<Iov> entitylist = iovRepository.findByIdTagName(tagname, req);
            return entitylist.stream().map(s -> mapper.map(s, IovDto.class))
                    .collect(Collectors.toList());
        }
        catch (final Exception e) {
            log.debug(
                    "Exception in retrieving iov list using IdByTagName expression and pagination...{}",
                    tagname);
            throw new CdbServiceException(
                    "Cannot find iovs by tag name and pagination: " + e.getMessage());
        }
    }

    /**
     * @param qry
     *            the Predicate
     * @param req
     *            the Pageable
     * @return List<IovDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<IovDto> findAllIovs(Predicate qry, Pageable req) throws CdbServiceException {
        try {
            Iterable<Iov> entitylist = null;
            if (qry == null) {
                entitylist = iovRepository.findAll(req);
            }
            else {
                entitylist = iovRepository.findAll(qry, req);
            }
            return StreamSupport.stream(entitylist.spliterator(), false)
                    .map(s -> mapper.map(s, IovDto.class)).collect(Collectors.toList());
        }
        catch (final Exception e) {
            log.error("Exception in retrieving iov list using predicate and pagination...");
            throw new CdbServiceException(
                    "Cannot find all iovs using predicate and pagination: " + e.getMessage());
        }
    }

    /**
     * @param tagname
     *            the String
     * @param snapshot
     *            the Date
     * @param groupsize
     *            the Long
     * @return List<BigDecimal>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<BigDecimal> selectGroupsByTagNameAndSnapshotTime(String tagname, Date snapshot,
            Long groupsize) throws CdbServiceException {
        try {
            log.debug("Search for iovs groups by tag name {} and snapshot time {}", tagname,
                    snapshot);
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
        catch (final Exception e) {
            log.error("Exception in retrieving iov groups list using tag {} and group size {}",
                    tagname, groupsize);
            throw new CdbServiceException("Cannot find iov groups by tag name: " + e.getMessage());
        }
    }

    /**
     * @param tagname
     *            the String
     * @param snapshot
     *            the Date
     * @param groupsize
     *            the Long
     * @return CrestBaseResponse
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @ProfileAndLog
    public CrestBaseResponse selectGroupDtoByTagNameAndSnapshotTime(String tagname, Date snapshot,
            Long groupsize) throws CdbServiceException {
        try {
            final List<BigDecimal> minsincelist = selectGroupsByTagNameAndSnapshotTime(tagname,
                    snapshot, groupsize);
            final List<IovDto> iovlist = minsincelist.stream().map(s -> new IovDto().since(s))
                    .collect(Collectors.toList());
            return new IovSetDto().resources(iovlist).size((long) iovlist.size());
        }
        catch (final Exception e) {
            log.error("Exception in retrieving iov groups list using tag and snapshot {}", tagname);
            throw new CdbServiceException("Cannot find iov groups by tag name: " + e.getMessage());
        }
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
     * @return List<IovDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<IovDto> selectIovsByTagRangeSnapshot(String tagname, BigDecimal since,
            BigDecimal until, Date snapshot) throws CdbServiceException {
        try {
            log.debug("Search for iovs by tag name {}  and range time {} -> {} using snapshot {}",
                    tagname, since, until, snapshot);
            Iterable<Iov> entities = null;
            if (snapshot == null) {
                entities = iovRepository.selectLatestByGroup(tagname, since, until);
            }
            else {
                entities = iovRepository.selectSnapshotByGroup(tagname, since, until, snapshot);
            }
            return StreamSupport.stream(entities.spliterator(), false)
                    .map(s -> mapper.map(s, IovDto.class)).collect(Collectors.toList());
        }
        catch (final Exception e) {
            log.debug("Exception in retrieving iov list using tag {} and snapshot and time range",
                    tagname);
            throw new CdbServiceException("Cannot find iov size by tag name: " + e.getMessage());
        }
    }

    /**
     * @param tagname
     *            the String
     * @param snapshot
     *            the Date
     * @return List<IovDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<IovDto> selectSnapshotByTag(String tagname, Date snapshot)
            throws CdbServiceException {
        try {
            log.debug("Search for snapshot by tag name {} using snapshot {}", tagname, snapshot);
            Iterable<Iov> entities = null;
            entities = iovRepository.selectSnapshot(tagname, snapshot);
            return StreamSupport.stream(entities.spliterator(), false)
                    .map(s -> mapper.map(s, IovDto.class)).collect(Collectors.toList());
        }
        catch (final Exception e) {
            log.error("Exception in retrieving iov list by tag using {}", tagname);
            throw new CdbServiceException(
                    "Cannot find iov size by tag name and snapshot: " + e.getMessage());
        }
    }

    /**
     * @param tagname
     *            the String
     * @return Long
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public Long getSizeByTag(String tagname) throws CdbServiceException {
        try {
            return iovgroupsrepo.getSize(tagname);
        }
        catch (final Exception e) {
            log.error("Exception in retrieving iov size using tag {}", tagname);
            throw new CdbServiceException("Cannot find iov size by tag name: " + e.getMessage());
        }
    }

    /**
     * @param tagname
     *            the String
     * @param snapshot
     *            the Date
     * @return Long
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public Long getSizeByTagAndSnapshot(String tagname, Date snapshot) throws CdbServiceException {
        try {
            return iovgroupsrepo.getSizeBySnapshot(tagname, snapshot);
        }
        catch (final Exception e) {
            log.debug("Exception in retrieving iov size using tag {} and snapshot {}", tagname,
                    snapshot);
            throw new CdbServiceException(
                    "Cannot find iov size by tag name and snapshot: " + e.getMessage());
        }
    }

    /**
     * @param tagname
     *            the String
     * @return List<TagSummaryDto>
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    public List<TagSummaryDto> getTagSummaryInfo(String tagname) throws CdbServiceException {
        try {
            return iovgroupsrepo.getTagSummaryInfo(tagname);
        }
        catch (final Exception e) {
            log.error("Exception in retrieving iov summary information for tag matching {}",
                    tagname);
            throw new CdbServiceException("Cannot find niovs by tag name: " + e.getMessage());
        }
    }

    /**
     * @param dto
     *            the IovDto
     * @return IovDto
     * @throws CdbServiceException
     *             If an Exception occurred
     */
    @Transactional
    public IovDto insertIov(IovDto dto) throws CdbServiceException {
        log.debug("Create iov from dto {}", dto);
        Iov tmpiov = null;
        Iov entity = null;
        final String tagname = dto.getTagName();
        try {
            entity = mapper.map(dto, Iov.class);
            log.debug("Verify if the same IOV is already stored with the same hash....");
            tmpiov = iovRepository.findBySinceAndTagNameAndHash(tagname, entity.getId().getSince(),
                    entity.getPayloadHash());
        }
        catch (final Exception e) {
            log.warn("Searching iov {} has not found anything...", dto);
        }
        if (entity == null) {
            throw new CdbServiceException("Cannot map entity to dto " + dto);
        }
        if (tmpiov != null) {
            log.debug("Found iov with the same Id and Hash...skip insertion....");
            throw new AlreadyExistsPojoException(tmpiov.toString());
        }
        try {
            // The IOV is not yet stored. Verify that the tag exists before inserting it.
            final Optional<Tag> tg = tagRepository.findById(tagname);
            if (tg.isPresent()) {
                final Tag t = tg.get();
                t.setModificationTime(null);
                final Tag updtag = tagRepository.save(t);
                entity.setTag(updtag);
                entity.getId().setTagName(tagname);
                final Iov saved = iovRepository.save(entity);
                log.debug("Saved entity: {}", saved);
                final IovDto dtoentity = mapper.map(saved, IovDto.class);
                log.debug("Returning iovDto: {}", dtoentity);
                return dtoentity;
            }
            throw new CdbServiceException("Unkown tag : " + tagname);

        }
        catch (final Exception e) {
            log.error("Exception in storing iov {}", dto);
            throw new CdbServiceException("Cannot store iov : " + e.getMessage());
        }
    }

}
