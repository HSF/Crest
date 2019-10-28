package hep.crest.data.security.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import hep.crest.data.config.DatabasePropertyConfigurator;

/**
 * @author formica
 *
 */
@Entity
@Table(name = "CREST_USERS", schema = DatabasePropertyConfigurator.SCHEMA_NAME)
public class CrestUser {

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
    public CrestUser() {
    }

    /**
     * @param username
     *            the String
     * @param password
     *            the String
     */
    public CrestUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @return String
     */
    @Id
    @Column(name = "CREST_USRID", unique = true, nullable = false, length = 100)
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the String
     * @return
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return String
     */
    @Column(name = "CREST_USRNAME", unique = true, nullable = false, length = 100)
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the String
     * @return
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return String
     */
    @Column(name = "CREST_USRPSS", unique = true, nullable = false, length = 100)
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the String
     * @return
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
