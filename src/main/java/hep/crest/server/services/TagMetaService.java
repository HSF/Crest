/**
 *
 */
package hep.crest.server.services;

import hep.crest.server.data.pojo.TagMeta;
import hep.crest.server.data.repositories.TagMetaRepository;
import hep.crest.server.exceptions.AbstractCdbServiceException;
import hep.crest.server.exceptions.CdbNotFoundException;
import hep.crest.server.exceptions.ConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Optional;

/**
 * @author rsipos
 *
 */
@Service
@Slf4j
public class TagMetaService {

    /**
     * Repository.
     */
    @Autowired
    private TagMetaRepository tagmetaRepository;
    /**
     * Cache manager.
     */
    @Autowired
    private CacheManager cacheManager;

    /**
     * Find TagMeta.
     *
     * @param name
     * @return TagMeta
     * @throws AbstractCdbServiceException
     */
    public TagMeta find(String name) {
        log.debug("Search meta info for tag {}", name);
        try {
            TagMeta cached = getTagMetaFromCache(name);
            if (cached != null) {
                return cached;
            }
            TagMeta entity = tagmetaRepository.findByTagName(name).orElseThrow(
                    () -> new CdbNotFoundException("Cannot find meta info for tag " + name));
            cacheTagMeta(entity);
            return entity;
        }
        catch (CdbNotFoundException e) {
            log.error("Tag Meta not found: {}", name);
            cacheEviction(name);
            throw e;
        }
    }


    /**
     * Cache eviction method.
     * @param name
     */
    protected void cacheEviction(String name) {
        Cache cache = cacheManager.getCache("tagMetaCache");
        if (cache != null) {
            cache.evictIfPresent(name);  // Evict based on the 'name' key
        }
    }


    /**
     * Cache add method.
     * @param tagmeta the TagMeta entity
     */
    protected void cacheTagMeta(TagMeta tagmeta) {
        Cache cache = cacheManager.getCache("tagMetaCache");
        if (cache != null && tagmeta != null) {
            cache.put(tagmeta.getTagName(), tagmeta);  // Use tag.getName() as the key and the
            // entity as the value
        }
    }

    /**
     * Get entity from cache.
     *
     * @param name the Tag name
     * @return TagMeta the entity
     */
    protected TagMeta getTagMetaFromCache(String name) {
        Cache cache = cacheManager.getCache("tagMetaCache");
        if (cache != null) {
            return cache.get(name, TagMeta.class);  // Retrieve the cached entity by key
        }
        return null;
    }

    /**
     * Insert new tag meta data.
     *
     * @param entity
     *            the TagMeta
     * @return TagMeta
     * @throws AbstractCdbServiceException
     *             If an Exception occurred
     */
    public TagMeta insertTagMeta(TagMeta entity) {
        log.debug("Create tag meta data from entity {}", entity);
        final String name = entity.getTagName();
        Optional<TagMeta> opt = tagmetaRepository.findByTagName(name);
        if (opt.isPresent()) {
            log.debug("Cannot store tag meta {} : resource already exists.. ", name);
            throw new ConflictException(
                "Tag meta already exists for name " + name);
        }
        final TagMeta saved = tagmetaRepository.save(entity);
        log.debug("Saved entity: {}", saved);
        return saved;
    }

    /**
     * Update an existing tag meta data.
     *
     * @param entity
     *            the TagMeta
     * @return TagMeta
     * @throws AbstractCdbServiceException If an exception occurred.
     */
    @CacheEvict(value = "tagMetaCache", key = "#entity.getTagName()")
    @Transactional
    public TagMeta updateTagMeta(TagMeta entity) {
        log.debug("Update tag meta from entity {}", entity);
        Optional<TagMeta> opt = tagmetaRepository.findByTagName(entity.getTagName());
        if (opt.isEmpty()) {
            throw new CdbNotFoundException("Cannot find meta info for " + entity.getTagName());
        }
        TagMeta stored = opt.get();
        log.debug("Updating existing tag meta {}", stored);
        stored.setTagInfo(entity.getTagInfo())
                .setChansize(entity.getChansize())
                .setColsize(entity.getColsize())
                .setDescription(entity.getDescription());
        final TagMeta saved = tagmetaRepository.save(stored);
        log.debug("Updated entity: {}", saved);
        return saved;
    }

    /**
     * Remote tag meta.
     *
     * @param name the name
     * @throws AbstractCdbServiceException the cdb service exception
     */
    @CacheEvict(value = "tagMetaCache", key = "#name")
    @Transactional
    public void removeTagMeta(String name) {
        log.debug("Remove tag meta info for {}", name);
        Optional<TagMeta> opt = tagmetaRepository.findByTagName(name);
        if (opt.isEmpty()) {
            throw new CdbNotFoundException("Cannot find tag meta for tag name " + name);
        }
        tagmetaRepository.delete(opt.get());
        log.debug("Removed tag meta info for: {}", name);
    }
}
