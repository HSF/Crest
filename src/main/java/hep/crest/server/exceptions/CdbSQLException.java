/**
 * 
 */
package hep.crest.server.exceptions;

import jakarta.ws.rs.core.Response;

/**
 * @author formica
 *
 */
public class CdbSQLException extends AbstractCdbServiceException {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -8552538724531679765L;

    /**
     * @param message
     *            the String
     */
    public CdbSQLException(String message) {
        super(message);
    }

    /**
     * @param message
     *            the String
     * @param err
     *            the Throwable
     */
    public CdbSQLException(String message, Throwable err) {
        super(message, err);
    }

    /**
     * Create using a throwable.
     *
     * @param cause
     */
    public CdbSQLException(Throwable cause) {
        super(cause);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return "SQL " + super.getMessage();
    }

    /**
     * Associate an HTTP response code, in case this error needs to be sent to the client.
     *
     * @return the response status
     */
    @Override
    public Response.StatusType getResponseStatus() {
        return Response.Status.NOT_MODIFIED;
    }
    /**
     * Just put ERROR for every exception.
     *
     * @return the type of the exception.
     */
    @Override
    public String getType() {
        return "SQL_ERROR";
    }

}
