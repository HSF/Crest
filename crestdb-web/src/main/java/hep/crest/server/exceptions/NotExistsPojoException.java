/**
 * 
 */
package hep.crest.server.exceptions;

/**
 * @author formica
 *
 */
public class NotExistsPojoException extends Exception {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -8552538724531679765L;

    /**
     * @param string
     *            the String
     */
    public NotExistsPojoException(String string) {
        super(string);
    }

    /**
     * @param string
     *            the String
     * @param err
     *            the Throwable
     */
    public NotExistsPojoException(String string, Throwable err) {
        super(string, err);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.exceptions.CdbServiceException#getMessage()
     */
    @Override
    public String getMessage() {
        return "NotExistsPojoException: " + super.getMessage();
    }

}
