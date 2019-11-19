package hep.crest.server.security;

/**
 * @author formica
 *
 */
public class UserResource {

    /**
     * The id of the user.
     */
    private String id;
    /**
     * The user name.
     */
    private String username;
    /**
     * The password.
     */
    private String password;

    /**
     * Default ctor.
     */
    public UserResource() {
    }

    /**
     * @param username
     *            the String
     * @param password
     *            the String
     */
    public UserResource(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
