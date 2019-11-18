/**
 * 
 */
package hep.crest.data.config;

/**
 * @author aformic
 *
 */
public final class DatabasePropertyConfigurator {

    // Possible options here: CMS_CONDITIONS_002, ATLAS_PHYS_COND.
    // The value @SCHEMA_NAME@ could be used, but requires valid gradle code for
    // substitution.
    /**
     * The SCHEMA_NAME.
     */
    public static final String SCHEMA_NAME = "";

    /**
     * Private ctor.
     */
    private DatabasePropertyConfigurator() {
        // Used to hide the constructor
    }
}
