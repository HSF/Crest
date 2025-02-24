/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package hep.crest.server.exceptions;

import jakarta.ws.rs.core.Response;

/**
 * @author formica
 *
 */
public class PayloadEncodingException extends AbstractCdbServiceException {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = 1851049463533897275L;

    /**
     * @param string
     *            the String
     */
    public PayloadEncodingException(String string) {
        super(string);
    }

    /**
     * @param e
     *            the Exception
     */
    public PayloadEncodingException(Exception e) {
        super(e);
    }

    /**
     * Add context information.
     * @param message
     * @param err
     */
    public PayloadEncodingException(String message, Throwable err) {
        super(message, err);
    }

    /**
     * Add context information.
     * @param err
     */
    public PayloadEncodingException(Throwable err) {
        super(err);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return "Encoding error " + super.getMessage();
    }

    /**
     * Associate an HTTP response code, in case this error needs to be sent to the client.
     *
     * @return the response status
     */
    @Override
    public Response.StatusType getResponseStatus() {
        return Response.Status.NOT_ACCEPTABLE;
    }
    /**
     * Just put ERROR for every exception.
     *
     * @return the type of the exception.
     */
    @Override
    public String getType() {
        return "ENCODING_ERROR";
    }
}
