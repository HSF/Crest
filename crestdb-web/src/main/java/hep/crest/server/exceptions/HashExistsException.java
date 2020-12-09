/**
 * 
 */
package hep.crest.server.exceptions;

import hep.crest.data.exceptions.CdbServiceException;

/**
 * @author formica
 *
 */
public class HashExistsException extends CdbServiceException {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -8552538724531679765L;

    /**
     * @param message
     *            the String
     */
    public HashExistsException(String message) {
        super(message);
    }

    /**
     * @param message
     *            the String
     * @param err
     *            the Throwable
     */
    public HashExistsException(String message, Throwable err) {
        super(message, err);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return "HashExistsException: " + super.getMessage();
    }
}
