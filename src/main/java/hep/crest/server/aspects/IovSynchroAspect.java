/**
 * <<<<<<< HEAD
 */
package hep.crest.server.aspects;

import hep.crest.server.config.CrestProperties;
import hep.crest.server.data.pojo.Iov;
import hep.crest.server.data.pojo.Tag;
import hep.crest.server.data.pojo.TagSynchroEnum;
import hep.crest.server.services.IovService;
import hep.crest.server.services.TagService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.NotAuthorizedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * This class is an aspect: to see where it is called you should look to the annotation.
 *
 * @author formica
 */
@Aspect
@Component
@Slf4j
public class IovSynchroAspect {
    /**
     * Properties.
     */
    private CrestProperties cprops;

    /**
     * The user info.
     */
    private UserInfo userinfo;

    /**
     * Service.
     */
    private TagService tagService;
    /**
     * Service.
     */
    private IovService iovService;

    /**
     * Ctor using injection.
     * @param cprops
     * @param userinfo
     * @param tagService
     * @param iovService
     *
     */
    @Autowired
    IovSynchroAspect(CrestProperties cprops, UserInfo userinfo,
                            TagService tagService, IovService iovService) {
        this.cprops = cprops;
        this.userinfo = userinfo;
        this.tagService = tagService;
        this.iovService = iovService;
    }

    /**
     * Check synchronization.
     *
     * @param pjp    the joinpoint
     * @param entity the Tag
     * @return Object
     * @throws Throwable If an Exception occurred
     */
    @Around("execution(* hep.crest.server.services.IovService.insertIov(*)) && args(entity)"
            + " || execution(* hep.crest.server.services.IovService.storeIov(*)) && args(entity)")
    public Object checkSynchro(ProceedingJoinPoint pjp, Iov entity) throws Throwable {
        log.debug("Iov insertion should verify the tag synchronization type : {}", entity);
        Object retVal = null;
        Boolean allowedOperation = false;
        // If there is no or weak security activated then set allowed to true.
        if ("none".equals(cprops.getSecurity()) || "weak".equals(cprops.getSecurity())) {
            log.warn("security checks are disabled in this configuration....");
            allowedOperation = true;
        }
        else {
            // Check the authentication.
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String role = entity.getTag().getName().split("-")[0].toLowerCase();
            Boolean hasrole = userinfo.isUserInRole(auth, role);
            if (hasrole.equals(Boolean.TRUE) || entity.getTag().getName().startsWith("TEST")) {
                log.info("User is allowed to write IOVs into tag {}", entity.getTag().getName());
                allowedOperation = true;
            }
        }
        Boolean acceptTime = false;
        // Get synchro property
        if ("none".equals(cprops.getSynchro())) {
            log.warn("synchronization checks are disabled in this configuration....");
            acceptTime = true;
        }
        else if (Boolean.TRUE.equals(allowedOperation)) {
            // Synchronization aspect is enabled.
            Tag tagentity = null;
            tagentity = tagService.findOne(entity.getTag().getName());
            // Get synchro type from tag.
            acceptTime = evaluateCondition(tagentity, entity);
        }
        // Proceed if the time is compatible with the tag definition,
        // and if the user is allowed to write.
        if (acceptTime && allowedOperation) {
            // Check if iov exists: if so just update the insertion time.
            Iov s = overrideIov(entity);
            // Proceed if allowed.
            retVal = pjp.proceed(new Object[] {s});
        }
        else {
            log.warn("Not authorized, either you cannot write in this tag or synchro type is "
                    + "wrong: auth={} synchro={}", allowedOperation, acceptTime);
            throw new NotAuthorizedException("You cannot write iov {}", entity);
        }
        return retVal;
    }

    /**
     * Method to evaluate condition based on Tag synchronization type.
     * For the moment we always accept insertions. This shall change.
     *
     * @param tagentity the tag
     * @param entity    the iov
     * @return Boolean : True if the Iov should be accepted for insertion. False otherwise.
     */
    protected boolean evaluateCondition(Tag tagentity, Iov entity) {
        final String synchro = tagentity.getSynchronization();
        Boolean acceptTime = Boolean.FALSE;
        Iov latest = iovService.latest(tagentity.getName());
        //
        switch (TagSynchroEnum.valueOf(synchro)) {
            case SV:
                log.warn("Can only append IOVs....");
                if (latest == null
                        || latest.getId().getSince().compareTo(entity.getId().getSince()) <= 0) {
                    // Latest is before the new one.
                    log.info("IOV in insert has correct time respect to last IOV : {} > {}",
                            entity, latest);
                    acceptTime = true;
                }
                else {
                    // Latest is after the new one.
                    log.warn("IOV in insert has WRONG time respect to last IOV : {} < {}",
                            entity, latest);
                    acceptTime = false;
                }
                break;
            case UPDATE:
                log.warn("Can append data in case the since is after the end time of the tag");
                BigInteger endofval = tagentity.getEndOfValidity();
                if (endofval == null || endofval.compareTo(entity.getId().getSince()) <= 0) {
                    log.info("The since is after end of validity of the Tag");
                    acceptTime = true;
                }
                break;
            case NONE:
                log.warn("Can insert data in any case because it is an open tag");
                acceptTime = true;
                break;
            default:
                // Nothing here, synchro type is not implemented.
                // We should throw an exception here.
                // For the time being we accept the insertion.
                log.warn("Synchro type not found....Insertion is not allowed by default [FIXME]");
                log.warn("We allow insertion during development....");
                acceptTime = true;
                break;
        }
        return acceptTime;
    }

    /**
     * Method to override the Iov. If the iov exists, the insertion time is updated.
     *
     * @param entity the iov
     * @return Iov
     */
    protected Iov overrideIov(Iov entity) {
        // Check if iov exists
        Iov s = iovService.existsIov(
                entity.getTag().getName(), entity.getId().getSince(), entity.getPayloadHash());
        if (s != null) {
            log.warn("Iov already exists [tag,since,hash], update insertion time for: {}", entity);
            final Timestamp now = Timestamp.from(Instant.now());
            s.getId().setInsertionTime(now);
            iovService.updateIov(s);
            entity = s;
        }
        return entity;
    }
}
