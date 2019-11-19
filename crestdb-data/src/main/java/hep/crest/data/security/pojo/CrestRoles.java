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
@Table(name = "CREST_ROLES", schema = DatabasePropertyConfigurator.SCHEMA_NAME)
public class CrestRoles {

    /**
     * The role ID.
     */
    private String id;
    /**
     * The role name.
     */
    private String role;

    /**
     * Default ctor.
     */
    public CrestRoles() {
    }

    /**
     * @param id
     *            the String
     * @param role
     *            the String
     */
    public CrestRoles(String id, String role) {
        this.id = id;
        this.role = role;
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
    @Column(name = "CREST_USRROLE", unique = false, nullable = false, length = 100)
    public String getRole() {
        return role;
    }

    /**
     * @param role
     *            the String
     * @return
     */
    public void setRole(String role) {
        this.role = role;
    }

}
