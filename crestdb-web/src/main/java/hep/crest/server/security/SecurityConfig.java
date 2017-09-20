/**
 * 
 */
package hep.crest.server.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;

import hep.crest.data.config.CrestProperties;

/**
 * @author formica
 *
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CrestProperties cprops;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		log.debug("Configure http security rules");
		//SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		
		if (cprops.getSecurity().equals("active")) {
			http.authorizeRequests().antMatchers(HttpMethod.GET, "/**").permitAll()
				.antMatchers(HttpMethod.POST, "/**").hasRole("ADMIN")
				.and().httpBasic().and().csrf().disable();
		} else if (cprops.getSecurity().equals("weak")) {
			http.authorizeRequests().antMatchers("/**").permitAll()
			.and().httpBasic().and().csrf().disable();
		} else if (cprops.getSecurity().equals("reco")) {
			http.authorizeRequests().antMatchers(HttpMethod.POST,"/**").denyAll()
			.and().httpBasic().and().csrf().disable();
		} else if (cprops.getSecurity().equals("none")) {
			log.info("No security enabled for this server....");
			http.authorizeRequests().antMatchers("/**").permitAll()
			.and().csrf().disable();
		}
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		log.debug("Configure authentication manager");
		auth.inMemoryAuthentication().withUser("user").password("password").roles("USER").and().withUser("admin")
				.password("password").roles("ADMIN", "USER");
	}
}