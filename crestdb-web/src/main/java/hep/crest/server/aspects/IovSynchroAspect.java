/**
 * 
 */
package hep.crest.server.aspects;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.data.pojo.Tag;
import hep.crest.server.exceptions.NotExistsPojoException;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import hep.crest.swagger.model.IovDto;
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
     * @param dto
     *            the IovDto
     */
    @Before("execution(* hep.crest.server.services.IovService.insertIov(*)) && args(dto)")
    public void checkSynchro(IovDto dto) {
        log.debug("Iov insertion should verify the tag synchronization type : {}",
                dto.getTagName());
        // Get synchro property
        if ("none".equals(cprops.getSynchro())) {
            log.warn("synchronization checks are disabled in this configuration....");
            return;
        }
        // Synchronization aspect is enabled.
        Tag entity = null;
        try {
            entity = tagService.findOne(dto.getTagName());
        }
        catch (final NotExistsPojoException e) {
            log.error("Error checking synchronization, tag does not exists : {}", e);
            return;
        }
        
        // Get synchro type from tag.
        final String synchro = entity.getSynchronization();
        
        // Synchro for single version type. Can only append IOVs.
        if ("SV".equalsIgnoreCase(synchro)) {
            log.warn("Can only append IOVs....");
            IovDto latest = null;
            try {
                latest = iovService.latest(entity.getName(), "now", "ms");
            }
            catch (final CdbServiceException e) {
                log.error("Error checking SV synchronization : {}", e);
            }
            if (latest == null) {
                log.info("No iov could be retrieved");
            }
            else if (latest.getSince().compareTo(dto.getSince()) <= 0) {
                log.info("IOV in insert has correct time");
            }
            else {
                log.error("IOV in insert has wrong time....cannot insert");
            }
        }
        else {
            // Nothing here, synchro type is not implemented.
            log.debug("Synchro type not found....");
        }
    }
}
