/**
 * 
 */
package hep.crest.server.exceptions;

/**
 * @author formica
 *
 */
public class AlreadyExistsPojoException extends Exception {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -8552538724531679765L;

    /**
     * @param string
     *            the String
     */
    public AlreadyExistsPojoException(String string) {
        super(string);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.exceptions.CdbServiceException#getMessage()
     */
    @Override
    public String getMessage() {
        return "AlreadyExistsPojoException: " + super.getMessage();
    }

}
