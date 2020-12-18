package hep.crest.server.filters;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter is an example. It is not used for the moment.
 *
 * @author formica
 *
 */
public class AuthenticationFilter implements ContainerRequestFilter {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    /**
     * Authentication scheme.
     */
    private static final String AUTHENTICATION_SCHEME = "Basic";
    /**
     * Authorization property.
     */
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
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
     * The injection works basically only if we register correctly this filter in JerseyConfig.
     * This uninitialized statement is not passing sonarqube but it should not be corrected.
     * Resource.
     */
    @Context
    private ResourceInfo resourceInfo;

    
    /**
     * Default ctor.
     */
    public AuthenticationFilter() {
        if (resourceInfo == null) {
            log.error("Cannot get ResourceInfo from context");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.ws.rs.container.ContainerRequestFilter#filter(javax.ws.rs.container.
     * ContainerRequestContext)
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        log.info("Authentication filter operates in context....{}", requestContext.getMethod());
        final Method method = resourceInfo.getResourceMethod();
        // Access allowed for all
        if (!method.isAnnotationPresent(PermitAll.class)) {
            // Access denied for all
            if (method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }

            // Get request headers
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();
            headers.forEach((k, v) -> log.debug("key {} = {} ", k, v));

            // Fetch authorization header
            final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

            // If no authorization information present; block access
            if (authorization == null || authorization.isEmpty()) {
                log.error("There is no authorization property in the header....");
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }

            // Get encoded username and password
            final String encodedUserPassword = authorization.get(0)
                    .replaceFirst(AUTHENTICATION_SCHEME + " ", "");
            log.debug("found user and password: {}", encodedUserPassword);
            // Decode username and password
            String usernameAndPassword = null;
            try {
                usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword));
            }
            catch (final IllegalArgumentException e) {
                log.error("Cannot get username and password : {}", e.getMessage());
                requestContext.abortWith(SERVER_ERROR);
                return;
            }

            if (!isUserAuthenticated(usernameAndPassword)) {
                requestContext.abortWith(ACCESS_DENIED);
            }
            // Verify user access
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                final RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                final Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

                // Is user valid?
                if (!isUserAllowed(usernameAndPassword, rolesSet)) {
                    requestContext.abortWith(ACCESS_DENIED);
                }
            }
        }
    }

    /**
     * @param userpass
     *            the String
     * @return boolean
     */
    private boolean isUserAuthenticated(final String userpass) {
        // Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(userpass, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();

        // Verifying Username and password
        if ("reader".equalsIgnoreCase(username) && "password".equalsIgnoreCase(password)) {
            log.debug("Found reader user....");
            return true;
        }
        else if (!("admin".equalsIgnoreCase(username) && "password".equalsIgnoreCase(password))) {
            return false;
        }
        return false;
    }

    /**
     * @param userpass
     *            the String
     * @param rolesSet
     *            the Set<String>
     * @return boolean
     */
    private boolean isUserAllowed(final String userpass, final Set<String> rolesSet) {
        // Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(userpass, ":");
        final String username = tokenizer.nextToken();
        boolean isAllowed = false;

        // Step 1. Fetch password from database and match with password in argument
        // If both match then get the defined role for user from database and continue.
        // If not then return isAllowed [false]
        // Access the database and do this part yourself : get the user role
        // e.g.: userMgr.getUserRole(username)
        String userRole = "ADMIN";
        if ("reader".equalsIgnoreCase(username)) {
            userRole = "READER";
        }
        // Step 2. Verify user role
        if (rolesSet.contains(userRole)) {
            isAllowed = true;
        }
        return isAllowed;
    }
}
