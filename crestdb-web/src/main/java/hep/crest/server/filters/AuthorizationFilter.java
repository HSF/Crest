package hep.crest.server.filters;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collection;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * This filter is here as an example.
 *
 * @author formica
 *
 */
@Provider
// FIXME: the name binding seems not to work when annotation is applied at the
// level of the implementing class
// It is probably mandatory to bind it with the *Api classes.
//// @AuthorizationControl
public class AuthorizationFilter implements ContainerRequestFilter {

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Response for denied access.
     */
    private static final Response ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED)
            .build();
    /**
     * Response for forbidden access.
     */
    private static final Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN)
            .build();
    /**
     * Response for server access error.
     */
    private static final Response SERVER_ERROR = Response
            .status(Response.Status.INTERNAL_SERVER_ERROR).build();

    /**
     * Resource.
     */
    @Context
    private ResourceInfo resourceInfo;

    /**
     * Context.
     */
    @Context
    private SecurityContext securityContext;

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.ws.rs.container.ContainerRequestFilter#filter(javax.ws.rs.container.
     * ContainerRequestContext)
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        log.info("Authorization filter operates in context....{}", requestContext.getMethod());
        final Method method = resourceInfo.getResourceMethod();
        log.info("Authorization filter is called on method....{} {} ", method.getName(),
                resourceInfo.getResourceClass().getName());
        // Access allowed for all
        final Principal principal = securityContext.getUserPrincipal();
        log.debug("Found user {}", principal);
        if (principal == null) {
            requestContext.abortWith(ACCESS_DENIED);
        }
        final Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        for (final GrantedAuthority grantedAuthority : authorities) {
            log.info("User has authority : {}", grantedAuthority);
        }
        if (false) {
            requestContext.abortWith(ACCESS_FORBIDDEN);
        }
    }
}
