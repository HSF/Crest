/**
 * 
 */
package hep.crest.server.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import hep.crest.data.config.CrestProperties;
import hep.crest.swagger.model.TagDto;

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
public class TagSecurityAspect {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TagSecurityAspect.class);

    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;

    /**
     * @param dto
     *            the TagDto
     */
    @Before("execution(* hep.crest.server.services.TagService.insertTag(*)) && args(dto)")
    public void checkRole(TagDto dto) {
        log.debug("Tag insertion should verify the tag name : {}", dto.getName());
        // If there is no or weak security activated then return.
        if ("none".equals(cprops.getSecurity()) || "weak".equals(cprops.getSecurity())) {
            log.warn("security checks are disabled in this configuration....");
            return;
        }
        // Check the authentication.
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            // No authentication is present. It will be used to reject the request.
            log.debug(
                    "Stop execution....for the moment it only print this message...no action is taken");
        }
        else {
            // Retrieve user details.
            final UserDetails userDetails = (UserDetails) auth.getPrincipal();
            log.debug("Tag insertion should verify the role for user : {}",
                    userDetails == null ? "none" : userDetails);
            log.debug("For the moment we print all roles and filter on one role as an example...");
            if (userDetails != null) {
                // User details are available.
                userDetails.getAuthorities().stream()
                        .forEach(s -> log.debug("Role is {}", s.getAuthority()));
                // If ATLAS-CONDITIONS role is present, then it should allow the method.
                final GrantedAuthority[] tagroles = userDetails.getAuthorities().stream()
                        .filter(s -> s.getAuthority().startsWith("ATLAS-CONDITIONS"))
                        .toArray(GrantedAuthority[]::new);
                log.debug("Found list of roles of length {}", tagroles.length);
                // For the moment just print the roles.
                userDetails.getAuthorities().stream()
                        .filter(s -> s.getAuthority().startsWith("ATLAS-CONDITIONS"))
                        .forEach(s -> log.debug("Selected role is {}", s.getAuthority()));
            }
        }
    }

}
