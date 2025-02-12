package hep.crest.server.repositories.triggerdb;

import lombok.Data;

/**
 * Class to hold the components of a trigger DB URL.
 */
@Data
public class UrlComponents {
    /**
     * The schema.
     */
    private final String schema;
    /**
     * The table.
     */
    private final String table;
    /**
     * The id.
     */
    private final Long id;

}
