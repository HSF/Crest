package hep.crest.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityTestFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/**").authorizeHttpRequests(
                authorize -> authorize.anyRequest().permitAll()  // Allow all requests
        );
        http.csrf(AbstractHttpConfigurer::disable);  // Disable CSRF

        return http.build();
    }
}