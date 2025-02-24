package hep.crest.server.data.repositories.args;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class TagQueryArgs implements Serializable {
    /**
     * The tag name.
     */
    private String name;
    /**
     * The objectType.
     */
    private String objectType;
    /**
     * The TimeType.
     */
    private String timeType;
    /**
     * The description.
     */
    private String description;

}
