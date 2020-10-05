package hep.crest.server.security;

import hep.crest.data.config.CrestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Web security configuration.
 *
 * @version %I%, %G%
 * @author formica
 *
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Properties.
     */
    @Autowired
    private CrestProperties cprops;

    /**
     * The user search base for LDAP.
     */
    @Value("${USER_SEARCH_BASE}")
    private String userSearchBase;
    /**
     * The user dn patterns for LDAP.
     */
    @Value("${USER_DN_PATTERNS}")
    private String userDnPatterns;
    /**
     * The user search filter for LDAP.
     */
    @Value("${USER_SEARCH_FILTER}")
    private String userSearchFilter;

    /**
     * The group search base for LDAP.
     */
    @Value("${GROUP_SEARCH_BASE}")
    private String groupSearchBase;
    /**
     * The group search filter for LDAP.
     */
    @Value("${GROUP_SEARCH_FILTER}")
    private String groupSearchFilter;
    /**
     * The group role attribute for LDAP.
     */
    @Value("${GROUP_ROLE_ATTRIBUTE}")
    private String groupRoleAttribute;
    /**
     * The manager dn for LDAP.
     */
    @Value("${MANAGER_DN}")
    private String managerDn;
    /**
     * The manager dn password for LDAP.
     */
    @Value("${MANAGER_PASSWORD}")
    private String managerPassword;
    /**
     * The url for the authentication for LDAP.
     */
    @Value("${LDAP_AUTHENTICATOR_URL}")
    private String url;
    /**
     * The access.
     */
    @Value("${ACCESS}")
    private String access;

    /**
     * the audience.
     */
    @Value("${auth0.audience}")
    private String audience;

    /**
     * the issuer.
     */
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    /**
     *
     * @return the Decoder
     */
    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.security.config.annotation.web.configuration.
     * WebSecurityConfigurerAdapter#configure(org.springframework.security.config.
     * annotation.web.builders.HttpSecurity)
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.debug("Configure http security rules");

        if ("active".equals(cprops.getSecurity())) {
            http.authorizeRequests().antMatchers(HttpMethod.GET, "/**").permitAll()
                    .antMatchers(HttpMethod.POST, "/**").access("hasAuthority('ATLAS-CONDITIONS')")
                    .antMatchers(HttpMethod.DELETE, "/**").hasRole("GURU").and().httpBasic().and()
                    .csrf().disable();

        }
        else if ("none".equals(cprops.getSecurity())) {
            log.info("No security enabled for this server....");
            http.authorizeRequests().antMatchers("/**").permitAll().and().httpBasic().and().csrf()
                    .disable();
        }
        else if ("reco".equals(cprops.getSecurity())) {
            http.authorizeRequests().antMatchers(HttpMethod.POST, "/**").denyAll()
                    .antMatchers(HttpMethod.PUT, "/**").denyAll()
                    .antMatchers(HttpMethod.DELETE, "/**").denyAll().and().httpBasic().and().csrf()
                    .disable();
        }
        else if ("weak".equals(cprops.getSecurity())) {
            log.info("Low security enabled for this server....");
            http.authorizeRequests().antMatchers(HttpMethod.DELETE, "/**").hasRole("GURU")
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .antMatchers(HttpMethod.HEAD, "/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/**").permitAll()
                    .antMatchers(HttpMethod.POST, "/**").permitAll()
                    .antMatchers(HttpMethod.DELETE, "/admin/**").hasRole("GURU")
                    .antMatchers(HttpMethod.PUT, "/admin/**").hasRole("GURU").and().httpBasic()
                    .and().csrf().disable();
        }
        else if ("auth0".equals(cprops.getSecurity())) {
            log.info("Auth0 security enabled for this server....");
            http.authorizeRequests()
                    .antMatchers(HttpMethod.DELETE, "/**").hasAuthority("SCOPE_manage:all")
                    .antMatchers(HttpMethod.PUT, "/admin/**").hasAuthority("SCOPE_manage:all")
                    .antMatchers(HttpMethod.PUT, "/tags", "/iovs", "/payloads")
                        .hasAuthority("SCOPE_write:tags")
                    .antMatchers(HttpMethod.POST, "/tags", "/iovs", "/payloads")
                        .hasAuthority("SCOPE_write:tags")
                    .antMatchers(HttpMethod.GET, "/**").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
                    .and()
                    .oauth2ResourceServer().jwt();
        }
    }

//    /**
//     * @param auth
//     *            the AuthenticationManagerBuilder
//     * @throws Exception
//     *             If an Exception occurred
//     */
/*
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        log.debug("Configure authentication manager");
        if ("database".equals(cprops.getAuthenticationtype())) {
            auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        }
        else if ("ldap".equals(cprops.getAuthenticationtype())) {
            // here put LDAP
            log.debug("Use ldap authentication: {} {} {} {} ", url, managerDn, userSearchBase,
                    userDnPatterns);
            final LdapAuthoritiesPopulator ldapAuthoritiesPopulator = authoritiesPopulator(
                    contextSource());
            auth.ldapAuthentication().ldapAuthoritiesPopulator(ldapAuthoritiesPopulator)
                    .contextSource(contextSource()).userSearchBase(userSearchBase)
                    .userDnPatterns(userDnPatterns).userSearchFilter(userSearchFilter)
                    .rolePrefix("");
        }
        else {
            auth.inMemoryAuthentication().withUser("userusr").password("password").roles("user")
                    .and().withUser("adminusr").password("password").roles("admin", "user").and()
                    .withUser("guru").password("guru").roles("admin", "user", "GURU");
        }
        // for this check
        // http://www.programming-free.com/2015/09/spring-security-password-encryption.html?showComment=1502891898256
        // auth.userDetailsService(accountRepository)

    }
*/

    /**
     * @return LdapContextSource
     */
    @Bean
    public LdapContextSource contextSource() {
        final String ldapurl = url;
        final DefaultSpringSecurityContextSource context = new DefaultSpringSecurityContextSource(
                ldapurl);
        context.setUserDn(managerDn);
        context.setPassword(managerPassword);
        context.setReferral("follow");
        context.afterPropertiesSet();
        return context;
    }

    /**
     * @return LdapTemplate
     */
    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }

    /**
     * @param context
     *            the ContextSource
     * @return LdapAuthoritiesPopulator
     */
    @Bean(name = "ldapAuthoritiesPopulator")
    public LdapAuthoritiesPopulator authoritiesPopulator(ContextSource context) {
        log.debug("Instantiate authorities populator....");
        final LdapAuthoritiesPopulator ldp = new DefaultLdapAuthoritiesPopulator(context,
                groupSearchBase);
        ((DefaultLdapAuthoritiesPopulator) ldp).setSearchSubtree(true);
        ((DefaultLdapAuthoritiesPopulator) ldp).setGroupRoleAttribute(groupRoleAttribute);
        ((DefaultLdapAuthoritiesPopulator) ldp).setGroupSearchFilter(groupSearchFilter);
        ((DefaultLdapAuthoritiesPopulator) ldp).setRolePrefix("");
        return ldp;
    }

    /**
     * @return PasswordEncoder
     */
    @Bean(name = "dbPasswordEncoder")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
