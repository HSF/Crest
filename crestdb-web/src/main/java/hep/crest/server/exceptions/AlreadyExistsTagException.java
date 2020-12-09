/**
 * 
 */
package hep.crest.server.exceptions;

/**
 * @author formica
 *
 */
public class AlreadyExistsTagException extends AlreadyExistsPojoException {

    /**
     * @param string
     *            the String
     */
    public AlreadyExistsTagException(String string) {
        super(string);
    }

    /**
     * @param string
     *            the String
     * @param err
     *            the Throwable
     */
    public AlreadyExistsTagException(String string, Throwable err) {
        super(string, err);
    }

    /*
     * (non-Javadoc)
     *
     * @see hep.crest.data.exceptions.CdbServiceException#getMessage()
     */
    @Override
    public String getMessage() {
        return "AlreadyExistsIovException: " + super.getMessage();
    }

}
