/**
 * 
 */
package hep.crest.data.exceptions;

/**
 * @author formica
 *
 */
public class CdbServiceException extends RuntimeException {

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

    /**
     * @param message
     *            the String
     * @param err
     *            the Throwable
     */
    public CdbServiceException(String message, Throwable err) {
        super(message, err);
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
