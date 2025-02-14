package hep.crest.server.security;

import hep.crest.server.config.CrestProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod; // <-- Import this
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web security configuration. This is used only with profile different from keycloak.
 *
 * @version %I%, %G%
 * @author formica
 *
 */
@Profile({"!keycloak"})
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityDefaultConfig {

    /**
     * Properties.
     */
    private CrestProperties cprops;

    /**
     * Ctor for injection.
     * @param cprops
     */
    @Autowired
    SecurityDefaultConfig(CrestProperties cprops) {
        this.cprops = cprops;
    }

    /**
     * JwtDecoder. This is a fake one
     * @return JwtDecoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("https://your-jwk-set-uri.com/.well-known/jwks.json").build();
    }

    /**
     * Security filter chain.
     * @param http
     * @return SecurityFilterChain
     * @throws Exception
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Security configuration with profile: {} ", cprops.getSecurity());
        if ("weak".equals(cprops.getSecurity())) {
            log.info("Allow only GET requests....");
            http.authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(HttpMethod.GET, "/**").permitAll()  // Allow all GET requests
                    .anyRequest().denyAll()  // Deny all other requests
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }
        else {
            log.info("Allow all requests....");
            http.securityMatcher("/**").authorizeHttpRequests(
                    authorize -> authorize.anyRequest().permitAll()  // Allow all requests
            );
            http.csrf(AbstractHttpConfigurer::disable);  // Disable CSRF, only for testing purposes
        }
        return http.build();
    }
}
