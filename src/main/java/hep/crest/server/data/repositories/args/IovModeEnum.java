package hep.crest.server.data.repositories.args;

/**
 * The enum for the IOV query mode.
 *
 */
public enum IovModeEnum {
    /**
     * Iovs.
     */
    IOVS("iovs"),
    /**
     * Groups.
     */
    GROUPS("groups"),
    /**
     * AT a given time.
     */
    AT("attime"),
    /**
     * Ranges.
     */
    RANGES("ranges");

    /**
     * The status.
     */
    private String mode;

    /**
     * Default Ctor.
     *
     * @param mode
     *            the String
     */
    IovModeEnum(String mode) {
        this.mode = mode;
    }

    /**
     * @return String
     */
    public String mode() {
        return mode;
    }

}
