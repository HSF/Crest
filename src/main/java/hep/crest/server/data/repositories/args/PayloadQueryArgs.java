package hep.crest.server.data.repositories.args;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class PayloadQueryArgs implements Serializable {
    /**
     * The hash name.
     */
    private String hash;
    /**
     * The objectType.
     */
    private String objectType;
    /**
     * The size.
     */
    private Integer size;
}
