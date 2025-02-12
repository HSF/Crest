/**
 * 
 */
package hep.crest.server.exceptions;

import jakarta.ws.rs.core.Response;

/**
 * @author formica
 *
 */
public class CdbInternalException extends AbstractCdbServiceException {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -8552538724531679765L;

    /**
     * @param string
     *            the String
     */
    public CdbInternalException(String string) {
        super(string);
    }

    /**
     * @param string
     *            the String
     * @param err
     *            the Throwable
     */
    public CdbInternalException(String string, Throwable err) {
        super(string, err);
    }

    /**
     * @param err
     *            the Throwable
     */
    public CdbInternalException(Throwable err) {
        super(err);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.exceptions.CdbServiceException#getMessage()
     */
    @Override
    public String getMessage() {
        return "Internal error " + super.getMessage();
    }

    /**
     * Associate an HTTP response code, in case this error needs to be sent to the client.
     *
     * @return the response status
     */
    @Override
    public Response.StatusType getResponseStatus() {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }
    /**
     * Just put ERROR for every exception.
     *
     * @return the type of the exception.
     */
    @Override
    public String getType() {
        return "INTERNAL_SERVER_ERROR";
    }

}
