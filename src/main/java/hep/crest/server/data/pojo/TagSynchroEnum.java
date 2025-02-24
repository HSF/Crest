package hep.crest.server.data.pojo;

/**
 * The enum for the IOV query mode.
 *
 */
public enum TagSynchroEnum {
    /**
     * open.
     */
    OPEN("open"),
    /**
     * Single Version.
     */
    SV("sv"),
    /**
     * Ranges.
     */
    UPDATE("upd"),
    /**
     * ignored.
     */
    NONE("none");

    /**
     * The synchro mode.
     */
    private String mode;

    /**
     * Default Ctor.
     *
     * @param mode
     *            the String
     */
    TagSynchroEnum(String mode) {
        this.mode = mode;
    }

    /**
     * @return String
     */
    public String type() {
        return mode;
    }

}
