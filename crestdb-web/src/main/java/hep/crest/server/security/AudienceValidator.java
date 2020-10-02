package hep.crest.server.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The token validator.
 *
 * @author formica
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    /**
     * the audience.
     */
    private final String audience;

    /**
     * Constructor.
     *
     * @param audience
     */
    AudienceValidator(String audience) {
        this.audience = audience;
    }

    /**
     * Validate the token.
     *
     * @param jwt
     * @return the token validation result
     */
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);

        if (jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(error);
    }
}
