package hep.crest.server.filters;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import hep.crest.server.annotations.AuthorizationControl;

@Provider
// FIXME: the name binding seems not to work when annotation is applied at the level of the implementing class
// It is probably mandatory to bind it with the *Api classes.
////@AuthorizationControl
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
		log.info("Authorization filter is called on method...."+method.getName()+" "+resourceInfo.getResourceClass().getName());
		// Access allowed for all
//		if (method.isAnnotationPresent(AuthorizationControl.class)) {
			Principal principal = securityContext.getUserPrincipal();
			log.info("Found user "+principal);
//			HttpServletRequest req = (HttpServletRequest)requestContext.getRequest();
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
			for (GrantedAuthority grantedAuthority : authorities) {
				log.info("User has authority : "+grantedAuthority);
			}
			if (false) 
				requestContext.abortWith(ACCESS_FORBIDDEN);
			return;
//		}
	}
}
