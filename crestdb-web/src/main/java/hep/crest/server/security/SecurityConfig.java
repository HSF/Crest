/**
 * 
 */
package hep.crest.server.security;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import hep.crest.data.config.CrestProperties;

/**
 * @author formica
 *
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DataSource ds;

	@Autowired
	private CrestProperties cprops;

	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		log.debug("Configure http security rules");
		//SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		
		if (cprops.getSecurity().equals("active")) {
			http.authorizeRequests().antMatchers(HttpMethod.GET, "/**").permitAll()
				.antMatchers(HttpMethod.POST, "/**").hasRole("admin")
				.antMatchers(HttpMethod.DELETE,"/**").hasRole("GURU")
				.and().httpBasic().and().csrf().disable();
		} else if (cprops.getSecurity().equals("weak")) {
			http.authorizeRequests().antMatchers("/**").permitAll()
			.and().httpBasic().and().csrf().disable();
		} else if (cprops.getSecurity().equals("reco")) {
			http.authorizeRequests().antMatchers(HttpMethod.POST,"/**").denyAll()
			.and().httpBasic().and().csrf().disable();
		} else if (cprops.getSecurity().equals("none")) {
			log.info("No security enabled for this server....");
			http.authorizeRequests()
			.antMatchers(HttpMethod.DELETE,"/**").hasRole("GURU")
			.antMatchers(HttpMethod.OPTIONS,"/**").permitAll()
			.antMatchers(HttpMethod.HEAD,"/**").permitAll()
			.antMatchers(HttpMethod.GET,"/**").permitAll()
			.antMatchers(HttpMethod.POST,"/**").permitAll()
			.antMatchers(HttpMethod.DELETE, "/admin/**").hasRole("GURU")
			.antMatchers(HttpMethod.PUT, "/admin/**").hasRole("GURU")
			.and().httpBasic().and().csrf().disable();
		}
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		log.debug("Configure authentication manager");
		if (cprops.getAuthenticationtype().equals("database")) {
			auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		} else if (cprops.getAuthenticationtype().equals("ldap")) {
			// here put LDAP
		} else {
			auth.inMemoryAuthentication().withUser("user").password("password").roles("USER").and().withUser("admin")
				.password("password").roles("ADMIN", "USER").and().withUser("guru")
				.password("guru").roles("ADMIN", "USER","GURU");
		}
// for this check http://www.programming-free.com/2015/09/spring-security-password-encryption.html?showComment=1502891898256
//		auth.userDetailsService(accountRepository)

	}
	
	@Bean(name="passwordEncoder")
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}