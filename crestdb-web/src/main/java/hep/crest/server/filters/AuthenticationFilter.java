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

public class AuthenticationFilter implements ContainerRequestFilter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String AUTHORIZATION_PROPERTY = "Authorization";
	private static final String AUTHENTICATION_SCHEME = "Basic";
	private static final Response ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED).build();
	private static final Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN).build();
	private static final Response SERVER_ERROR = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		log.info("Authentication filter operates in context...."+requestContext.getMethod());
		Method method = resourceInfo.getResourceMethod();
		// Access allowed for all
		if (!method.isAnnotationPresent(PermitAll.class)) {
			// Access denied for all
			if (method.isAnnotationPresent(DenyAll.class)) {
				requestContext.abortWith(ACCESS_FORBIDDEN);
				return;
			}

			// Get request headers
			final MultivaluedMap<String, String> headers = requestContext.getHeaders();
			headers.forEach((k,v) -> log.debug("key "+k+" = "+v));
			
			// Fetch authorization header
			final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

			// If no authorization information present; block access
			if (authorization == null || authorization.isEmpty()) {
				log.error("There is no authorization property in the header....");
				requestContext.abortWith(ACCESS_DENIED);
				return;
			}

			// Get encoded username and password
			final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");
			log.debug("found user and password: "+encodedUserPassword);
			// Decode username and password
			String usernameAndPassword = null;
			try {
				usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword));
			} catch (Exception e) {
				requestContext.abortWith(SERVER_ERROR);
				return;
			}

			// Split username and password tokens
			final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
			final String username = tokenizer.nextToken();
			final String password = tokenizer.nextToken();

			// Verifying Username and password
			if ((username.equalsIgnoreCase("reader") && password.equalsIgnoreCase("password"))) {
				log.debug("Found reader user....");
			} else if (!(username.equalsIgnoreCase("admin") && password.equalsIgnoreCase("password"))) {
				requestContext.abortWith(ACCESS_DENIED);
				return;
			}

			// Verify user access
			if (method.isAnnotationPresent(RolesAllowed.class)) {
				RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
				Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

				// Is user valid?
				if (!isUserAllowed(username, password, rolesSet)) {
					requestContext.abortWith(ACCESS_DENIED);
					return;
				}
			}
		}
	}

	private boolean isUserAllowed(final String username, final String password, final Set<String> rolesSet) {
		boolean isAllowed = false;

		// Step 1. Fetch password from database and match with password in argument
		// If both match then get the defined role for user from database and continue;
		// else return isAllowed [false]
		// Access the database and do this part yourself
		// String userRole = userMgr.getUserRole(username);
		String userRole = "ADMIN";
		if (username.equalsIgnoreCase("reader")) {
			userRole="READER";
		}
		// Step 2. Verify user role
		if (rolesSet.contains(userRole)) {
			isAllowed = true;
		}
		return isAllowed;
	}
}
