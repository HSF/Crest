package hep.crest.server.security;

import org.springframework.stereotype.Component;

/**
 * A fake security repository.
 *
 * @author formica
 *
 */
@Component
public class AccountRepository {

    /**
     * @param username
     *            the String
     * @return UserResource
     */
    public UserResource findByUsername(String username) {
        if (username.equals("reader")) {
            return new UserResource("reader", "r_password");
        }
        else if (username.equals("admin")) {
            return new UserResource("admin", "a_password");
        }
        else if (username.equals("guest")) {
            return new UserResource("guest", "g_password");
        }
        else {
            return null;
        }
    }
}
