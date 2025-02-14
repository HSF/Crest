package hep.crest.server.aspects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of UserInfo interface.
 * @author formica
 */
@Slf4j
@Component
public class UserInfoImpl implements UserInfo {

    @Override
    public String getUserId(Authentication auth) {
        String clientId = "TEST";

        if (auth == null) {
            log.warn("Stop execution... No authentication is present.");
            return clientId;
        }

        // Check if authentication is an OAuth2 token (for JWT or Opaque tokens)
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            log.info("JWT Principal: {}", jwtAuth.getPrincipal());

            if (jwt != null) {
                log.debug("Found JWT token: {}", jwt.getTokenValue());
                // Access claims or any other token data
                Map<String, Object> claims = jwt.getClaims();
                clientId = getClientId(claims);
            }
        }
        else if (auth instanceof OAuth2AuthenticationToken oauth2Auth) {
            OAuth2User oauth2User = oauth2Auth.getPrincipal();
            log.info("OAuth2 Principal: {}", oauth2User);

            if (oauth2User != null) {
                Map<String, Object> attributes = oauth2User.getAttributes();
                log.debug("Found OAuth2 user attributes: {}", attributes);
                clientId = getClientId(attributes);
            }
        }

        return clientId;
    }

    @Override
    public Boolean isUserInRole(Authentication auth, String role) {
        final Principal user = (Principal) auth.getPrincipal();
        log.info("Verify the role for user : {} ",
                user == null ? "none" : user);
        Collection<? extends GrantedAuthority> roles = auth.getAuthorities();
        if (user != null) {
            // Search if tagname is in list of roles.
            String crestrole = "ROLE_crest-" + role;
            if (Boolean.TRUE.equals(isRole(crestrole, roles))) {
                return Boolean.TRUE;
            }
            if (Boolean.TRUE.equals(isRole("ROLE_crest-admin", roles))) {
                return Boolean.TRUE;
            }
            if (Boolean.TRUE.equals(isRole("ROLE_crest-developers", roles))) {
                return Boolean.TRUE;
            }
        }
        // Seems that the user is not in the role.
        roles.forEach(s -> log.debug("Selected role is {}", s.getAuthority()));
        return Boolean.FALSE;
    }

    /**
     *
     * @param rname
     * @param roles
     * @return Boolean
     */
    protected Boolean isRole(String rname, Collection<? extends GrantedAuthority> roles) {
        Optional<? extends GrantedAuthority> userRole = roles.stream()
                .filter(r -> r.getAuthority().equalsIgnoreCase(rname)).findFirst();
        if (userRole.isPresent()) {
            log.info("User is in role {} : he can create tag with name {}",
                    userRole.get().getAuthority(),
                    rname);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * Get the client ID from other claims.
     *
     * @param otherClaims
     * @return String
     */
    protected String getClientId(Map<String, Object> otherClaims) {
        String clientid = "TEST";
        if (otherClaims != null) {
            for (Map.Entry<String, Object> entry : otherClaims.entrySet()) {
                log.info("Found claim : {} ", entry);
                if ("clientId".equals(entry.getKey())) {
                    clientid = (String) entry.getValue();
                }
            }
        }
        return clientid;
    }
}
