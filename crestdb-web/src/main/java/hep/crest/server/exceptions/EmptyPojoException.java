/**
 * 
 */
package hep.crest.server.exceptions;

import hep.crest.data.exceptions.CdbServiceException;

/**
 * @author formica
 *
 */
public class EmptyPojoException extends CdbServiceException {

    /**
     * Serializer.
     */
    private static final long serialVersionUID = -8552538724531679765L;

    /**
     * @param string
     *            the String
     */
    public EmptyPojoException(String string) {
        super(string);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.exceptions.CdbServiceException#getMessage()
     */
    @Override
    public String getMessage() {
        return "EmptyPojoException: " + super.getMessage();
    }

}
