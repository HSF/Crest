package hep.crest.server.data.repositories.args;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Accessors(fluent = true)
public class GtagQueryArgs implements Serializable {
    /**
     * The tag name.
     */
    private String name;
    /**
     * The snapshot time.
     */
    private Timestamp snapshotTime;
    /**
     * The insertion time.
     */
    private Timestamp insertionTime;
    /**
     * The until time.
     */
    private BigDecimal validity;
    /**
     * The workflow name.
     */
    private String workflow;
    /**
     * The release name.
     */
    private String release;
    /**
     * The type name.
     */
    private String type;
    /**
     * The description.
     */
    private String description;
    /**
     * The scenario.
     */
    private String scenario;

}
