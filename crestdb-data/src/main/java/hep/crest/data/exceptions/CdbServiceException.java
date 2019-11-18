/**
 * 
 */
package hep.crest.data.exceptions;

/**
 * @author formica
 *
 */
public class CdbServiceException extends Exception {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -8552538724531679765L;

    /**
     * @param message
     *            the String
     */
    public CdbServiceException(String message) {
        super(message);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return "CdbServiceException: " + super.getMessage();
    }
}
