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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import hep.crest.data.config.CrestProperties;
import hep.crest.swagger.model.TagDto;


/**
 * @author formica
 *
 */
@Aspect
@Component
public class TagSecurityAspect {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CrestProperties cprops;

	@Before("execution(* hep.crest.server.services.TagService.insertTag(*)) && args(dto)")
	public void checkRole(TagDto dto) {
		log.debug("Tag insertion should verify the tag name :"+dto.getName());
		if (cprops.getSecurity().equals("none") || cprops.getSecurity().equals("weak")) {
			log.warn("security checks are disabled in this configuration....");
			return;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			log.debug("Stop execution....for the moment it only print this message...no action is taken");
		} else {
			User user = (User) auth.getPrincipal();
			log.debug("Tag insertion should verify the role for user :"+((user == null) ? "none" : user.getUsername()));
			user.getAuthorities().stream().forEach(s -> log.debug("Role is "+s.getAuthority()));
		}
	}
	

}
