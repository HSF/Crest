package hep.crest.server.serializers;

/**
 * Enumeration for time arguments.
 *
 * @author formica
 *
 */
public enum ArgTimeUnit {
    /**
     * msec.
     */
    MS("ms"),
    /**
     * sec.
     */
    SEC("sec"),
    /**
     * Run number.
     */
    RUN("run"),
    /**
     * Run-lumi string.
     */
    RUN_LUMI("run-lumi"),
    /**
     * custom. Will require a date format from the user.
     */
    CUSTOM("custom"),
    /**
     * COOL nsec or run/lumi format.
     */
    COOL("cool"),
    /**
     * Number with no interpretation.
     */
    NUMBER("number"),
    /**
     * iso.
     */
    ISO("iso");

    /**
     * The status.
     */
    private String value;

    /**
     * Default Ctor.
     *
     * @param status
     *            the String
     */
    ArgTimeUnit(String status) {
        this.value = status;
    }

    /**
     * @return String
     */
    public String value() {
        return value;
    }
}
