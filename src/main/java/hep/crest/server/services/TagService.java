/**
 *
 */
package hep.crest.server.services;

import hep.crest.server.controllers.PageRequestHelper;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.pojo.TagMeta;
import hep.crest.server.data.repositories.IovGroupsCustom;
import hep.crest.server.data.repositories.IovRepository;
import hep.crest.server.data.repositories.TagMetaRepository;
import hep.crest.server.data.repositories.TagRepository;
import hep.crest.server.data.repositories.args.TagQueryArgs;
import hep.crest.server.exceptions.AbstractCdbServiceException;
import hep.crest.server.exceptions.CdbNotFoundException;
import hep.crest.server.exceptions.ConflictException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author rsipos
 */
@Service
@Slf4j
@Getter
public class TagService {
    /**
     * Repository.
     */
    private TagRepository tagRepository;
    /**
     * Repository.
     */
    private IovRepository iovRepository;
    /**
     * Repository.
     */
    private IovService iovService;
    /**
     * Repository.
     */
    private PayloadService payloadService;
    /**
     * Service.
     */
    private TagMetaService tagMetaService;

    /**
     * Repository.
     */
    private TagMetaRepository tagMetaRepository;

    /**
     * Repository.
     */
    private IovGroupsCustom iovGroupsCustom;

    /**
     * Helper.
     */
    private PageRequestHelper prh;

    /**
     * Cache manager.
     */
    private CacheManager cacheManager;

    /**
     * Ctors with injected services.
     * @param tagRepository
     * @param cacheManager
     * @param iovService
     * @param payloadService
     * @param tagMetaService
     *
     */
    @Autowired
    TagService(TagRepository tagRepository, CacheManager cacheManager, IovService iovService,
            PayloadService payloadService, TagMetaService tagMetaService) {
        this.tagRepository = tagRepository;
        this.cacheManager = cacheManager;
        this.iovService = iovService;
        this.iovRepository = iovService.getIovRepository();
        this.prh = iovService.getPrh();
        this.iovGroupsCustom = iovService.getIovGroupsCustom();
        this.payloadService = payloadService;
        this.tagMetaService = tagMetaService;
        this.tagMetaRepository = tagMetaService.getTagmetaRepository();
    }

    /**
     * @param name the tag name
     * @return Tag
     * @throws AbstractCdbServiceException If object was not found
     */
    public Tag findOne(String name) throws AbstractCdbServiceException {
        log.debug("Search for tag by Id...{}", name);
        try {
            Tag cached = getTagFromCache(name);
            if (cached != null) {
                log.debug("Tag found in cache: {}", cached);
                return cached;
            }
            Tag entity = tagRepository.findById(name).orElseThrow(() -> new CdbNotFoundException(
                    "Tag not found: " + name));
            cacheTag(entity);
            return entity;
        }
        catch (CdbNotFoundException e) {
            log.error("Tag not found: {}", name);
            cacheEviction(name);
            throw e;
        }
    }

    /**
     * Cache eviction method.
     * @param name
     */
    protected void cacheEviction(String name) {
        Cache cache = cacheManager.getCache("tagCache");
        if (cache != null) {
            cache.evictIfPresent(name);  // Evict based on the 'name' key
        }
    }


    /**
     * Cache add method.
     * @param tag the Tag entity
     */
    protected void cacheTag(Tag tag) {
        Cache cache = cacheManager.getCache("tagCache");
        if (cache != null && tag != null) {
            cache.put(tag.getName(), tag);  // Use tag.getName() as the key and the entity as the value
        }
    }

    /**
     * Get entity from cache.
     *
     * @param name the Tag name
     * @return Tag the entity
     */
    protected Tag getTagFromCache(String name) {
        Cache cache = cacheManager.getCache("tagCache");
        if (cache != null) {
            return cache.get(name, Tag.class);  // Retrieve the cached entity by key
        }
        return null;
    }


    /**
     * Select Tags.
     *
     * @param args
     * @param preq
     * @return Page of Tag
     */
    public Page<Tag> selectTagList(TagQueryArgs args, Pageable preq) {
        Page<Tag> entitylist = null;
        if (preq == null) {
            String sort = "name:ASC";
            preq = prh.createPageRequest(0, 1000, sort);
        }
        entitylist = tagRepository.findTagList(args, preq);
        log.trace("Retrieved list of tags {}", entitylist);
        return entitylist;
    }


    /**
     * @param entity the Tag
     * @return Tag
     * @throws ConflictException If an Exception occurred because pojo exists
     */
    @Transactional
    public Tag insertTag(Tag entity) throws ConflictException {
        log.debug("Create Tag from {}", entity);
        final Optional<Tag> tmpt = tagRepository.findById(entity.getName());
        if (tmpt.isPresent()) {
            log.warn("Tag {} already exists.", tmpt.get());
            throw new ConflictException(
                    "Tag already exists for name " + entity.getName());
        }
        final Tag saved = tagRepository.save(entity);
        log.debug("Saved entity: {}", saved);
        return saved;
    }

    /**
     * Update an existing tag.
     *
     * @param entity the Tag
     * @return TagDto of the updated entity.
     * @throws AbstractCdbServiceException If an Exception occurred
     */
    @CacheEvict(value = "tagCache", key = "#entity.getName()")
    @Transactional
    public Tag updateTag(Tag entity) throws AbstractCdbServiceException {
        log.debug("Update tag from dto {}", entity);
        try {
            final Tag toupd = tagRepository.findById(entity.getName()).orElseThrow(
                    () -> new CdbNotFoundException(
                            "Tag does not exists for name " + entity.getName()));
            toupd.setDescription(entity.getDescription()).setObjectType(entity.getObjectType())
                    .setSynchronization(entity.getSynchronization())
                    .setEndOfValidity(entity.getEndOfValidity())
                    .setLastValidatedTime(entity.getLastValidatedTime())
                    .setTimeType(entity.getTimeType());
            final Tag saved = tagRepository.save(toupd);
            log.debug("Updated entity: {}", saved);
            return saved;
        }
        catch (CdbNotFoundException e) {
            log.error("updateTag error for : {}", entity.getName());
            Objects.requireNonNull(cacheManager.getCache("tagCache")).evict(entity.getName());
            throw e;
        }
    }

    /**
     * @param name the String
     * @throws AbstractCdbServiceException If an Exception occurred
     */
    @CacheEvict(value = "tagCache", key = "#name")
    @Transactional
    public void removeTag(String name) throws AbstractCdbServiceException {
        log.debug("Remove tag {} after checking if IOVs are present", name);
        try {
            Tag remTag = tagRepository.findById(name).orElseThrow(
                    () -> new CdbNotFoundException("Tag does not exists for name " + name));
            // Remove meta information associated with the tag.
            log.debug("Removing meta info on tag {}", remTag);
            Optional<TagMeta> opt = tagMetaRepository.findByTagName(name);
            if (opt.isPresent()) {
                tagMetaService.removeTagMeta(name);
            }
            // Start removing IOVs
            log.debug("Removing tag {}", remTag);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            // Verify the IOV content size
            int pageSize = 2000; // Batch size for deletion
            Pageable pageable = PageRequest.of(0, pageSize);
            Page<Iov> iovsPage;
            int pageIndex = 0;
            do {
                log.debug("Processing iov page of size {} : {}", pageSize, pageIndex);
                // Fetch the current page of IOVs
                iovsPage = iovRepository.findByIdTagName(name, pageable);
                // Process the current batch of IOVs
                List<Iov> iovList = iovsPage.getContent();
                List<String> hashList = iovService.removeIovList(iovList);
                CompletableFuture<Void> future = payloadService.removePage(hashList, name);
                futures.add(future);
                if (futures.size() >= 5) {
                    // Verify that all future task did end
                    log.debug("Wait for payloads to be removed by {} tasks", futures.size());
                    for (CompletableFuture<Void> f : futures) {
                        f.join();  // This blocks until the task completes
                    }
                    log.debug("Payload removal finished for {} tasks", futures.size());
                    futures.clear();
                }
                pageIndex++;
                // Continue using the same page (page 0), as removed items won't appear again
            } while (!iovsPage.isEmpty()); // Break if there are no more IOVs to process
            // Payload are now removed
            log.debug("Payloads have been removed");
            tagRepository.deleteById(name);
            log.debug("Removed entity: {}", name);
        }
        catch (AbstractCdbServiceException e) {
            log.error("Tag removal exception: {}", name);
            Objects.requireNonNull(cacheManager.getCache("tagCache")).evict(name);
            throw e;
        }
    }

    /**
     * Update the end of validity of a tag.
     * @param name
     * @param endtime
     * @throws AbstractCdbServiceException
     */
    @CacheEvict(value = "tagCache", key = "#name")
    public void updateModificationTime(String name, BigDecimal endtime) {
        // Change the end time in the tag.
        try {
            Tag tagEntity = tagRepository.findById(name).orElseThrow(
                    () -> new CdbNotFoundException("Tag does not exists for name " + name));
            tagEntity.setEndOfValidity(
                    (endtime != null) ? endtime.toBigInteger() : BigInteger.ZERO);
            // Update the modification time.
            tagEntity.setModificationTime(Date.from(Instant.now()));
            // Update the tag.
            this.updateTag(tagEntity);
        }
        catch (CdbNotFoundException e) {
            log.error("updateModificationTime error for tag: {}", name);
            Objects.requireNonNull(cacheManager.getCache("tagCache")).evict(name);
            throw e;
        }
    }
}
