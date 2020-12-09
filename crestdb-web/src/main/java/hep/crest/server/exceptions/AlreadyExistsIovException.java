/**
 * 
 */
package hep.crest.server.exceptions;

/**
 * @author formica
 *
 */
public class AlreadyExistsIovException extends AlreadyExistsPojoException {

    /**
     * @param string
     *            the String
     */
    public AlreadyExistsIovException(String string) {
        super(string);
    }

    /**
     * @param string
     *            the String
     * @param err
     *            the Throwable
     */
    public AlreadyExistsIovException(String string, Throwable err) {
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
