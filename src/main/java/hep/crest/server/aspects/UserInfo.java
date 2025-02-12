package hep.crest.server.aspects;

import org.springframework.security.core.Authentication;

/**
 * This interface allows to retrieve the user information from the security context.
 *
 * @author formica
 *
 */
public interface UserInfo {

    /**
     * @param auth
     *           the authentication object.
     * @return the user id.
     */
    String getUserId(Authentication auth);

    /**
     * @param auth
     *           the authentication object.
     * @param role
     *          the role to check.
     * @return the flag.
     */
    Boolean isUserInRole(Authentication auth, String role);
}
