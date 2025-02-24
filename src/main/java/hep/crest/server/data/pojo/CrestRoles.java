package hep.crest.server.data.pojo;

import hep.crest.server.config.DatabasePropertyConfigurator;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author formica
 *
 */
@Entity
@Table(name = "CREST_ROLES", schema = DatabasePropertyConfigurator.SCHEMA_NAME)
@Data
public class CrestRoles {

    /**
     * The role ID.
     */
    @Id
    @Column(name = "CREST_USRID", unique = true, nullable = false, length = 100)
    private String id;
    /**
     * The role name.
     */
    @Column(name = "CREST_USRROLE", unique = false, nullable = false, length = 100)
    private String role;

}
