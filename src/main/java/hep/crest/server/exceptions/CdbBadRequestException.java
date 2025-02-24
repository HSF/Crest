/**
 * 
 */
package hep.crest.server.exceptions;

import jakarta.ws.rs.core.Response;

/**
 * @author formica
 *
 */
public class CdbBadRequestException extends AbstractCdbServiceException {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -8552538724531679765L;

    /**
     * @param string
     *            the String
     */
    public CdbBadRequestException(String string) {
        super(string);
    }

    /**
     * @param string
     *            the String
     * @param err
     *            the Throwable
     */
    public CdbBadRequestException(String string, Throwable err) {
        super(string, err);
    }

    /**
     * @param err
     *            the Throwable
     */
    public CdbBadRequestException(Throwable err) {
        super(err);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.exceptions.CdbServiceException#getMessage()
     */
    @Override
    public String getMessage() {
        return "Bad request " + super.getMessage();
    }

    /**
     * Associate an HTTP response code, in case this error needs to be sent to the client.
     *
     * @return the response status
     */
    @Override
    public Response.StatusType getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }

    /**
     * Just put ERROR for every exception.
     *
     * @return the type of the exception.
     */
    @Override
    public String getType() {
        return "BAD_REQUEST";
    }
}
