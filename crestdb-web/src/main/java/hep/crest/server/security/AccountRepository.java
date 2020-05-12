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
        UserResource user = null;
        if ("reader".equals(username)) {
            user = new UserResource("reader", "r_password");
        }
        else if ("admin".equals(username)) {
            user = new UserResource("admin", "a_password");
        }
        else if ("guest".equals(username)) {
            user = new UserResource("guest", "g_password");
        }
        return user;
    }
}
