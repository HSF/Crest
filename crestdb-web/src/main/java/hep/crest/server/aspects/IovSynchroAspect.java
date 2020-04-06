/**
 * 
 */
package hep.crest.server.aspects;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.pojo.Iov;
import hep.crest.data.pojo.Tag;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author formica
 *
 */
@Aspect
@Component
public class IovSynchroAspect {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IovSynchroAspect.class);

    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;

    /**
     * Service.
     */
    @Autowired
    private TagService tagService;
    /**
     * Service.
     */
    @Autowired
    private IovService iovService;

    /**
     * Check synchronization.
     * @param entity
     *            the Iov
     */
    @Before("execution(* hep.crest.server.services.IovService.insertIov(*)) && args(entity)")
    public void checkSynchro(Iov entity) {
        log.debug("Iov insertion should verify the tag synchronization type : {}",
                entity.getTag().getName());
        // Get synchro property
        if ("none".equals(cprops.getSynchro())) {
            log.warn("synchronization checks are disabled in this configuration....");
            return;
        }
        // Synchronization aspect is enabled.
        Tag tagentity = null;
        try {
            tagentity = tagService.findOne(entity.getTag().getName());
        }
        catch (final NotExistsPojoException e) {
            log.error("Error checking synchronization, tag does not exists : {}", e);
            return;
        }
        
        // Get synchro type from tag.
        final String synchro = tagentity.getSynchronization();
        
        // Synchro for single version type. Can only append IOVs.
        if ("SV".equalsIgnoreCase(synchro)) {
            log.warn("Can only append IOVs....");
            Iov latest = null;
            latest = iovService.latest(tagentity.getName(), "now", "ms");
            if (latest == null) {
                log.info("No iov could be retrieved");
            }
            else if (latest.getId().getSince().compareTo(entity.getId().getSince()) <= 0) {
                log.info("IOV in insert has correct time respect to last IOV : {} > {}", entity, latest);
            }
            else {
                log.warn("IOV in insert has WRONG time respect to last IOV : {} < {}", entity, latest);
            }
        }
        else {
            // Nothing here, synchro type is not implemented.
            log.debug("Synchro type not found....");
        }
    }
}
