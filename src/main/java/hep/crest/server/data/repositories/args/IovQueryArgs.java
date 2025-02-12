package hep.crest.server.data.repositories.args;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;

@Data
@Accessors(fluent = true)
public class IovQueryArgs implements Serializable {
    /**
     * The tag name.
     */
    private String tagName;
    /**
     * The since time.
     */
    private BigInteger since;
    /**
     * The until time.
     */
    private BigInteger until;
    /**
     * The snapshot date.
     */
    private Timestamp snapshot;
    /**
     * The query mode.
     * Can be ranges, iovs, groups.
     */
    private IovModeEnum mode;
    /**
     * The hash of the payload.
     */
    private String hash;
    /**
     * The timeformat used in input.
     */
    private String timeformat;

    /**
     * Verify that relevant args are not null.
     * Return True if arguments are NULL, False otherwise.
     * @param method
     * @return Boolean (True means the argument are wrong).
     */
    public boolean checkArgsNull(String method) {
        Boolean status = Boolean.TRUE;
        if (method != null && !method.equalsIgnoreCase("MONITOR") && (
                tagName == null || tagName.contains("%"))) {
            return status;
        }

        if (tagName == null && hash == null) {
            // The tag cannot be null.
            return status;
        }
        if (since != null && until != null) {
            status = Boolean.FALSE;
        }
        else if (since != null) {
            // If only since is given, suppose that this is a query to select a unique IOV.
            status = Boolean.FALSE;
            mode = IovModeEnum.AT;
        }
        return status;
    }
}
