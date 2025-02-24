/**
 * 
 */
package hep.crest.server.exceptions;

import jakarta.ws.rs.core.Response;

/**
 * @author formica
 *
 */
public abstract class AbstractCdbServiceException extends RuntimeException {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -8552538724531679765L;

    /**
     * @param message
     *            the String
     */
    protected AbstractCdbServiceException(String message) {
        super(message);
    }
    /**
     * @param cause the Internal Exception.
     */
    protected AbstractCdbServiceException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     *            the String
     * @param err
     *            the Throwable
     */
    protected AbstractCdbServiceException(String message, Throwable err) {
        super(message, err);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return "error message => " + super.getMessage();
    }


    /**
     * Associate an HTTP response code, in case this error needs to be sent to the client.
     * @return the response status
     */
    public abstract Response.StatusType getResponseStatus();

    /*
     * The type of the exception: ERROR, INFO, ...
     */
    public abstract String getType();
}
