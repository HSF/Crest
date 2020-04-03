/**
 * 
 */
package hep.crest.server.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hep.crest.data.config.CrestProperties;
import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import hep.crest.swagger.model.IovDto;
import hep.crest.swagger.model.TagDto;

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
    private final Logger log = LoggerFactory.getLogger(this.getClass());

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
     * The execution insertion point is the method insertIov of the IovService. The idea is that when a new IOV is
     * inserted, then we can verify the policy to adopt by checking the TAG synchronization field.
     * Here some examples:
     *  - SV : IOVs can only be appended
     *  - ...: yet to be defined
     * We should complete this aspect in order to implement a mechanism to STOP the insertion in case of not
     * conformance with the policy. May be one should use the joinpoint or modify the dto argument.
     * @param dto
     *            the IovDto
     */
    @Before("execution(* hep.crest.server.services.IovService.insertIov(*)) && args(dto)")
    public void checkSynchro(IovDto dto) {
        log.debug("Iov insertion should verify the tag synchronization type : {}",
                dto.getTagName());
        // Verify if synchronization policy is requested
        if ("none".equals(cprops.getSynchro())) {
            log.warn("synchronization checks are disabled in this configuration....");
            return;
        }
        // Synchro policy is active.
        TagDto tagdto = null;
        try {
            // Load the tag
            tagdto = tagService.findOne(dto.getTagName());
        }
        catch (final CdbServiceException e) {
            log.error("Error checking synchronization : {}", e.getMessage());
        }
        if (tagdto == null) {
            log.debug("Cannot find synchro for null tag");
            return;
        }
        // Check the synchro policy
        final String synchro = tagdto.getSynchronization();
        if ("SV".equalsIgnoreCase(synchro)) {
            log.warn("Can only append IOVs....");
            IovDto latest = null;
            try {
                latest = iovService.latest(tagdto.getName(), "now", "ms");
            }
            catch (final CdbServiceException e) {
                log.error("Error checking SV synchronization : {}", e.getMessage());
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
            log.debug("Synchro type not found....");
        }
    }
}
