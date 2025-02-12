/**
 *
 */
package hep.crest.server.aspects;

import hep.crest.server.config.CrestProperties;
import hep.crest.server.data.pojo.Tag;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.NotAuthorizedException;

/**
 * Aspect to be used for security.
 * It checks the role of the user when executing some insertion actions.
 *
 * @version %I%, %G%
 * @author formica
 *
 */
@Aspect
@Component
@Slf4j
public class TagSecurityAspect {
    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;
    /**
     * The user info.
     */
    @Autowired
    private UserInfo userinfo;

    /**
     * @param pjp
     *            the joinpoint
     * @param entity
     *            the Tag
     * @return Object
     * @throws Throwable If an Exception occurred
     */
    @Around("execution(* hep.crest.server.services.TagService.insertTag(*)) && args(entity) "
            + " || execution(* hep.crest.server.services.TagService.updateTag(*)) && args(entity)")
    public Object checkRole(ProceedingJoinPoint pjp, Tag entity) throws Throwable {
        log.debug("Tag insertions security control for : {}", entity);
        Object retVal = null;
        // If there is no or weak security activated then return.
        if ("none".equals(cprops.getSecurity()) || "weak".equals(cprops.getSecurity())) {
            log.warn("security checks are disabled in this configuration....");
            retVal = pjp.proceed();
        }
        else {
            // Check the authentication.
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String clientid = userinfo.getUserId(auth);
            String role = entity.getName().split("-")[0].toLowerCase();
            Boolean hasrole = userinfo.isUserInRole(auth, role);
            if (hasrole || entity.getName().startsWith("TEST")) {
                retVal = pjp.proceed();
            }
            else {
                log.warn("Cannot use tag {} for clientId {}", entity, clientid);
                throw new NotAuthorizedException("Cannot write tag " + entity.getName());
            }
        }
        return retVal;
    }

}
