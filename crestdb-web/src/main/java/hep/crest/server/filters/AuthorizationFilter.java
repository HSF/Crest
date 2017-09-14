package hep.crest.server.filters;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hep.crest.server.annotations.AuthorizationControl;

@Provider
@AuthorizationControl
public class AuthorizationFilter implements ContainerRequestFilter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private static final Response ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED).build();
	private static final Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN).build();
	private static final Response SERVER_ERROR = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

	@Context
	private ResourceInfo resourceInfo;

	@Context 
	private SecurityContext securityContext;
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		log.info("Authorization filter operates in context...."+requestContext.getMethod());
		Method method = resourceInfo.getResourceMethod();
		// Access allowed for all
		if (method.isAnnotationPresent(AuthorizationControl.class)) {
			Principal principal = securityContext.getUserPrincipal();
			HttpServletRequest req = (HttpServletRequest)requestContext.getRequest();

			if (false) 
				requestContext.abortWith(ACCESS_FORBIDDEN);
			return;
		}
	}
}
