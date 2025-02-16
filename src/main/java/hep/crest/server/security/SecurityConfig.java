package hep.crest.server.security;

import hep.crest.server.config.CrestProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web security configuration. This is used only with profile keycloak.
 *
 * @author formica
 * @version %I%, %G%
 */
@Profile({"keycloak"})
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    /**
     * Properties.
     */
    private CrestProperties cprops;

    /**
     * Ctor with injected properties.
     * @param cprops the properties.
     */
    @Autowired
    public SecurityConfig(CrestProperties cprops) {
        this.cprops = cprops;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        if ("active".equals(cprops.getSecurity())) {
            log.info("Security is active for this server....");

        }
        else if ("none".equals(cprops.getSecurity())) {
            log.info("No security enabled for this server....");
            http.securityMatcher("/**")
                    .authorizeHttpRequests(authorize -> authorize.anyRequest()
                            .permitAll())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }

        return http.build();
    }
}
